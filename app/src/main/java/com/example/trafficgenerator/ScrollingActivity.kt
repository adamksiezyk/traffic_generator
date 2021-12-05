package com.example.trafficgenerator

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import com.example.trafficgenerator.databinding.ActivityScrollingBinding
import com.example.trafficgenerator.dto.GetTasksResponseDTO
import com.example.trafficgenerator.dto.TaskDTO
import com.example.trafficgenerator.scripts.AsyncTaskExecutor
import com.example.trafficgenerator.serverapi.ServerApi
import com.github.kittinunf.result.failure
import com.github.kittinunf.result.success
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors
import kotlin.concurrent.fixedRateTimer

class ScrollingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScrollingBinding
    private lateinit var logTextView: TextView
    private lateinit var asyncTaskExecutor: AsyncTaskExecutor
    private lateinit var serverApi: ServerApi
    private lateinit var keepAliveTimer: Timer

    @ObsoleteCoroutinesApi
    private val asyncNetworkScope = CoroutineScope(newSingleThreadContext("networkThread"))
    private var loggedIn: Boolean = false

    @SuppressLint("SimpleDateFormat")
    private val dateFormat: SimpleDateFormat = SimpleDateFormat("HH:mm:ss")

    /*
        Callback for the websocket when new task was received, which will get all tasks and filter the new one and add it to the execution queue
     */
    @RequiresApi(Build.VERSION_CODES.N)
    private fun newTaskReceived(task: TaskDTO, uuid: String, token: String) {
        asyncNetworkScope.launch {
            val result = serverApi.getTasks(token, uuid)
            result.success {
                val tasks = it.filter { it.id == task.taskId }
                if (tasks.isEmpty()) {
                    appendStringToLog("Failed to get task ${task.taskId} from all tasks")
                } else {
                    asyncTaskExecutor.addTaskToExecutionQueue(tasks.first(), uuid, token, ::taskFinished)
                }
            }
            result.failure {
                appendStringToLog("Failed to get tasks: $it")
            }
        }

    }

    /*
        Callback for the task executor to handle a finished task.
     */
    private fun taskFinished(task: GetTasksResponseDTO, uuid: String, token: String) {
        appendTaskToLog(task)

        asyncNetworkScope.launch {
            val response = serverApi.uploadTaskResult(token, uuid, task)
            response.success {
                appendStringToLog("Uploading task ${task.id} result succeeded")
            }
            response.failure {
                appendStringToLog("Uploading task ${task.id} result failed")
            }
        }
    }

    private fun appendTaskToLog(taskResult: GetTasksResponseDTO) {
        runOnUiThread {
            logTextView.append("\n${dateFormat.format(Date())}:\n\t${taskResult.id}\n\t${taskResult.taskType}\n\t${taskResult.status}\n\t${taskResult.fileUrl}\n\t${taskResult.orderStart}\n\t${taskResult.orderEnd}")
        }
    }

    private fun appendStringToLog(string: String) {
        runOnUiThread {
            logTextView.append("\n" + dateFormat.format(Date()) + ":" + string)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        asyncTaskExecutor = AsyncTaskExecutor(Executors.newSingleThreadExecutor())

        binding = ActivityScrollingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(findViewById(R.id.toolbar))
        binding.toolbarLayout.title = title
        binding.fab.setOnClickListener {
            openLoginActivity()
        }
        logTextView = findViewById(R.id.logger_textview)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            5225 -> {
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        val username = (data.extras?.get("username") as SpannableString).toString()
                        val password = (data.extras?.get("password") as SpannableString).toString()
                        val ipAddress = (data.extras?.get("ipAddress") as SpannableString).toString()
                        serverApi = ServerApi(applicationContext, ipAddress)

                        if (data.extras?.get("login") as Boolean) {
                            //If the requested operation is login

                            val uuid = data.extras?.get("uuid") as String
                            appendStringToLog("Login form data:")
                            appendStringToLog("\nUsername:\t$username\nPassword:\t$password")

                            asyncNetworkScope.launch {
                                appendStringToLog("login as $username with pw $password and uuid $uuid")
                                val response = serverApi.login(username, password, uuid)
                                response.success {
                                    onSuccessfulLogin()
                                    val token = it.token
                                    getSharedPreferences("tgr_prefs", Context.MODE_PRIVATE).edit {
                                        this.putString("username", username)
                                        this.putString("password", password)
                                        this.putString("ipAddress", ipAddress)
                                        this.putString("token", token)
                                        commit()
                                    }
                                    serverApi.listenForTasks(token, uuid, ::newTaskReceived)
                                }
                                response.failure {
                                    appendStringToLog("Login failed: $it")
                                }
                            }

                        } else {
                            //Requested operation is register
                            val deviceName = (data.extras?.get("deviceName") as SpannableString).toString()
                            appendStringToLog("Register form data:")
                            appendStringToLog("\nUsername:\t$username\nPassword:\t$password\nName:\t$deviceName")

                            asyncNetworkScope.launch {
                                appendStringToLog("register as $username with pw $password name $deviceName")
                                val response = serverApi.register(username, password, deviceName)
                                response.success {
                                    onSuccessfulLogin()
                                    val uuid = it.uuid
                                    val token = it.token
                                    getSharedPreferences("tgr_prefs", Context.MODE_PRIVATE).edit {
                                        this.putString("username", username)
                                        this.putString("password", password)
                                        this.putString("deviceName", deviceName)
                                        this.putString("ipAddress", ipAddress)
                                        this.putString("uuid", uuid)
                                        this.putString("token", token)
                                        commit()
                                    }
                                     serverApi.listenForTasks(token, uuid, ::newTaskReceived)
                                }
                                response.failure {
                                    appendStringToLog("Registration failed: $it")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun openLoginActivity() {
        val myIntent = Intent(this, LoginActivity::class.java)
        startActivityForResult(myIntent, 5225)
    }

    private fun onSuccessfulLogin() {
        binding.fab.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(applicationContext,R.color.aghGreen))
        binding.fab.setImageResource(R.drawable.ic_logged_in)

        keepAliveTimer = fixedRateTimer("keepAlive", initialDelay = 1000*60*10, period = 1000*60*10) {
            asyncNetworkScope.launch {
                serverApi.sendKeepAlive()
            }
        }
        binding.fab.setOnClickListener {
            logOut()
        }
        loggedIn = true
    }

    private fun logOut() {
        appendStringToLog("Logging out")
        keepAliveTimer.cancel()
        serverApi.close()
        binding.fab.setOnClickListener {
            openLoginActivity()
        }
        binding.fab.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(applicationContext,R.color.aghRed)))
        binding.fab.setImageResource(R.drawable.ic_logged_out)
        loggedIn = false

        // Remove the temporary token from the device
        getSharedPreferences("tgr_prefs", Context.MODE_PRIVATE).edit {
            this.remove("token")
            commit()
        }
    }
}