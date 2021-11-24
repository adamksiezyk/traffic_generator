package com.example.trafficgenerator

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.trafficgenerator.serverapi.ServerApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val username = "android"
        val password = "androidandroid"
        val name = "app1"
        val uuid = "2c12c75b-119b-4f37-a60b-28eae0957c02"
        val serverApi = ServerApi(applicationContext)

        GlobalScope.launch {
            val registerResult = (if (uuid == null) serverApi.register(username, password, name) else
                serverApi.login(username, password, uuid))
            val tasksResult = serverApi.getTasks(registerResult.get().token, registerResult.get().uuid)
            println(registerResult.get())
            println(tasksResult.get().contentToString())
        }
    }
}