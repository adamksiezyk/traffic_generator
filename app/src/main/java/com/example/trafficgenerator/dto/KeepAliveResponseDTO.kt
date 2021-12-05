package com.example.trafficgenerator.dto

import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.Gson

data class KeepAliveResponseDTO(
    val uuid: String,
    val deviceType: String,
    val deviceModel: String,
    val lastConnection: String,
    val status: String
) {
    class Deserializer : ResponseDeserializable<KeepAliveResponseDTO> {
        override fun deserialize(content: String): KeepAliveResponseDTO =
            Gson().fromJson(content, KeepAliveResponseDTO::class.java)
    }
}
