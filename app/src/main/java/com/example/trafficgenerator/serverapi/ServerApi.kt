package com.example.trafficgenerator.serverapi

import android.content.Context
import android.util.Log
import com.example.trafficgenerator.R
import com.example.trafficgenerator.dto.GetTasksResponseDTO
import com.example.trafficgenerator.dto.LoginResponseDTO
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.coroutines.awaitObjectResult
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import com.google.gson.Gson

class ServerApi(private val context: Context) {
    private val logTag: String = context.getString(R.string.server_api_tag)

    init {
        FuelManager.instance.basePath = context.getString(R.string.server)
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

//    suspend fun listenForTasks(callback: Callback) {
//        val task: GetTasksResponseDTO
//    }
}
