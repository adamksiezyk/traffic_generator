package com.example.trafficgenerator.dto

import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.Gson

data class TaskDTO(val id: Long, val type: String) {
    class Deserializer : ResponseDeserializable<TaskDTO> {
        override fun deserialize(content: String): TaskDTO =
            Gson().fromJson(content, TaskDTO::class.java)
    }
}