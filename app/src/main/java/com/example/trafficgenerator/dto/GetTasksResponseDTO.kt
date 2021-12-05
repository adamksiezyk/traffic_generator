package com.example.trafficgenerator.dto

import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.Gson

data class GetTasksResponseDTO(
    val id: Long,
    val taskType: String,
    val status: String,
    val fileUrl: String?,
    val orderStart: String,
    val orderEnd: String?,
    val device: Device
) {
    data class Device(
        val uuid: String,
        val deviceType: String,
        val deviceModel: String
    )

    class ArrayDeserializer : ResponseDeserializable<Array<GetTasksResponseDTO>> {
        override fun deserialize(content: String): Array<GetTasksResponseDTO> =
            Gson().fromJson(content, Array<GetTasksResponseDTO>::class.java)
    }

    class Deserializer : ResponseDeserializable<GetTasksResponseDTO> {
        override fun deserialize(content: String): GetTasksResponseDTO =
            Gson().fromJson(content, GetTasksResponseDTO::class.java)
    }

    class Serializer {
        fun serialize(content: GetTasksResponseDTO): String =
            Gson().toJson(content, GetTasksResponseDTO::class.java)
    }
}
