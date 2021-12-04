package com.example.trafficgenerator.serverapi

import android.content.Context
import android.util.Log
import com.example.trafficgenerator.R
import com.example.trafficgenerator.dto.GetTasksResponseDTO
import com.example.trafficgenerator.dto.LoginResponseDTO
import com.example.trafficgenerator.dto.TaskDTO
import com.example.trafficgenerator.scripts.log
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.coroutines.awaitObjectResult
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import com.google.gson.Gson
import io.reactivex.disposables.Disposable
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.StompHeader

class ServerApi(private val context: Context, private val ipAddress: String) {
    private var listeningSocket: StompClient? = null
    private var listenForTasksTopic: Disposable? = null
    private val logTag: String = context.getString(R.string.server_api_tag)

    init {
        FuelManager.instance.basePath = "http://$ipAddress"
    }

    fun close() {
        stopListeningForTasks()
        disconnectListeningSocket()
    }

    private fun Request.addJsonBodyHeader(): Request =
        header("Content-Type" to "application/json")

    suspend fun register(username: String, password: String, name: String): Result<LoginResponseDTO, FuelError> {
        Log.i(logTag, "Registering new device: $name")
        val registerURL = context.getString(R.string.register)
        val bodyJson = Gson().toJson(
            mapOf(
                "login" to username,
                "password" to password,
                "name" to name
            )
        ).toString()
        return registerURL
            .httpPost()
            .addJsonBodyHeader()
            .body(bodyJson)
            .awaitObjectResult(LoginResponseDTO.Deserializer())
    }

    suspend fun login(username: String, password: String, uuid: String): Result<LoginResponseDTO, FuelError> {
        Log.i(logTag, "Logging in device: $uuid")
        val loginURL = context.getString(R.string.login)
        val bodyJson = Gson().toJson(
            mapOf(
                "login" to username,
                "password" to password,
                "uuid" to uuid
            )
        ).toString()
        return loginURL
            .httpPost()
            .addJsonBodyHeader()
            .body(bodyJson)
            .awaitObjectResult(LoginResponseDTO.Deserializer())
    }

    private fun Request.addAuthorizationHeader(token: String): Request =
        header("Authorization" to "Bearer $token")

    private fun Request.addUUIDHeader(uuid: String): Request =
        header("device-uuid" to uuid)

    suspend fun getTasks(token: String, uuid: String): Result<Array<GetTasksResponseDTO>, FuelError> {
        Log.i(logTag, "Getting tasks for device: $uuid")
        val getTasksURL = context.getString(R.string.get_tasks)

        return getTasksURL
            .httpGet()
            .addJsonBodyHeader()
            .addAuthorizationHeader(token)
            .addUUIDHeader(uuid)
            .awaitObjectResult(GetTasksResponseDTO.Deserializer())
    }

    private fun connectListeningSocket(token: String) {
        if (listeningSocket == null) {
            Log.i(logTag, "Creating listening socket")
            listeningSocket = Stomp.over(
                Stomp.ConnectionProvider.OKHTTP,
                "ws://$ipAddress${context.getString(R.string.device_web_socket)}"
            )
        }
        if (listeningSocket?.isConnected == false) {
            Log.i(logTag, "Connecting listening socket")
            listeningSocket?.connect(listOf(StompHeader("Authorization", "Bearer $token")))
        }
    }

    private fun disconnectListeningSocket() {
        Log.i(logTag, "Disconnecting listening socket")
        listeningSocket?.disconnect()
    }

    fun listenForTasks(token: String, uuid: String, callback: (TaskDTO, String, String) -> (Unit)) {
        connectListeningSocket(token)
        Log.i(logTag, "Listening for tasks")
        listenForTasksTopic =
            listeningSocket?.topic("${context.getString(R.string.listen_for_tasks)}$uuid")?.subscribe { data ->
                Log.i(logTag, "Received task")
                callback(TaskDTO.Deserializer().deserialize(data.payload), uuid, token)
            }
    }

    fun stopListeningForTasks() {
        Log.i(logTag, "Stopping listening for tasks")
        listenForTasksTopic?.dispose()
    }
}
