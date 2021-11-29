package com.example.trafficgenerator

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.trafficgenerator.serverapi.ServerApi
import com.example.trafficgenerator.scripts.FtpClient
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import android.view.View
import android.widget.Button
import java.io.File

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // storing ID of the button
        // in a variable
        val button = findViewById<Button>(R.id.button_ftp)
        var ftp = FtpClient("10.0.2.2", 22, "fate", "fate")

        Thread(
            Runnable {
                ftp.establishConnection()
            }
        ).start()

        // operations to be performed
        // when user tap on the button
        button?.setOnClickListener() {
            Thread(
                Runnable {
                    val fileName = "install_server-6a9a542f"
                    val appDataPath = "/data/data/com.example.ftp5g/"
                    ftp.upload(File("${appDataPath}code_cache/$fileName"))
                    ftp.download(fileName, File("${appDataPath}${fileName}"))
                }
            ).start()
        }

//        val username = "android"
//        val password = "androidandroid"
//        val name = "app1"
//        val uuid = "2c12c75b-119b-4f37-a60b-28eae0957c02"
//        val serverApi = ServerApi(applicationContext)
//
//        GlobalScope.launch {
//            val registerResult = (if (uuid == null) serverApi.register(username, password, name) else
//                serverApi.login(username, password, uuid))
//            val tasksResult = serverApi.getTasks(registerResult.get().token, registerResult.get().uuid)
//            println(registerResult.get())
//            println(tasksResult.get().contentToString())
//        }
    }
}