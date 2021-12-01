package com.example.trafficgenerator

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Build
import android.text.SpannableString
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import com.example.trafficgenerator.dto.GetTasksResponseDTO
import com.example.trafficgenerator.scripts.AsyncTaskExecutor
import com.example.trafficgenerator.databinding.ActivityScrollingBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext

class ScrollingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScrollingBinding
    private lateinit var logTextView: TextView
    private lateinit var asyncTaskExecutor: AsyncTaskExecutor
    @ObsoleteCoroutinesApi
    private val asyncNetworkScope = CoroutineScope(newSingleThreadContext("name"))
    //private val serverApi: ServerApi = ServerApi(applicationContext)
    private var loggedIn : Boolean = false

    @SuppressLint("SimpleDateFormat")
    private val dateFormat: SimpleDateFormat = SimpleDateFormat("HH:mm:ss")

    /*
        Callback for the websocket when new task was received, which will add the task to the execution queue
     */
    @RequiresApi(Build.VERSION_CODES.N)
    private fun newTaskReceived(task: GetTasksResponseDTO) {
        asyncTaskExecutor.addTaskToExecutionQueue(task, ::taskFinished)
    }

    /*
        Callback for the task executor to handle a finished task.
     */
    private fun taskFinished(task: GetTasksResponseDTO) {
        // TODO: pass it to websocket
        appendTaskToLog(task)
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
                        //val data.extras?.get("ipAddress") as String

                        if (data.extras?.get("login") as Boolean) {
                            //If the requested operation is login

                            val uuid = data.extras?.get("uuid")
                            appendStringToLog("Login form data:")
                            appendStringToLog("\nUsername:\t$username\nPassword:\t$password")

                            asyncNetworkScope.launch {
                                Thread.sleep(1500)
                                appendStringToLog("login as $username with pw $password and uuid $uuid")
//                                val response = serverApi.login(username, password, uuid)
//                               if (response.component1() is LoginResponseDTO) {
//                                    onSuccessfulLogin()
//                                }
                            }

                        } else {
                            //Requested operation is register
                            val deviceName = (data.extras?.get("deviceName") as SpannableString).toString()
                            appendStringToLog("Register form data:")
                            appendStringToLog("\nUsername:\t$username\nPassword:\t$password\nName:\t$deviceName")

                            asyncNetworkScope.launch {
                                Thread.sleep(4000)
                                appendStringToLog("register as $username with pw $password name $deviceName")
//                                val response = serverApi.register(username, password, deviceName)
//                                if (response.component1() is LoginResponseDTO) {
//                                    onSuccessfulLogin()
//                                    getSharedPreferences("tgr_prefs", Context.MODE_PRIVATE).edit {
//                                        this.putString("uuid", response.component1().uuid)
//                                        this.putString("token", response.component1().token)
//                                        commit()
//                                    }
//                                }
                            }
                        }

                        //TODO: Remove fake tasks
                        newTaskReceived(GetTasksResponseDTO(0L, "sample", "none", "ftp://192.168.0.101", "started", "ended", GetTasksResponseDTO.Device("deadbeef", "Devi", "Pixel 4")))
                        newTaskReceived(GetTasksResponseDTO(0L, "sample", "none", "ftp://192.168.0.101", "started", "ended", GetTasksResponseDTO.Device("deadbeef", "Devi", "Pixel 4")))
                        newTaskReceived(GetTasksResponseDTO(0L, "sample", "none", "ftp://192.168.0.101", "started", "ended", GetTasksResponseDTO.Device("deadbeef", "Devi", "Pixel 4")))
                        newTaskReceived(GetTasksResponseDTO(0L, "sample", "none", "ftp://192.168.0.101", "started", "ended", GetTasksResponseDTO.Device("deadbeef", "Devi", "Pixel 4")))

                        // TODO: Call this based on result from websocket
                        onSuccessfulLogin()
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

        binding.fab.setOnClickListener {
            logOut()
        }
        loggedIn = true
    }

    private fun logOut() {
        appendStringToLog("Logging out")
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