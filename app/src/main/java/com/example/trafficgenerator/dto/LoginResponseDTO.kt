package com.example.trafficgenerator.dto

import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.Gson

data class LoginResponseDTO(val token: String, val uuid: String) {
    class Deserializer : ResponseDeserializable<LoginResponseDTO> {
        override fun deserialize(content: String): LoginResponseDTO =
            Gson().fromJson(content, LoginResponseDTO::class.java)
    }
}
