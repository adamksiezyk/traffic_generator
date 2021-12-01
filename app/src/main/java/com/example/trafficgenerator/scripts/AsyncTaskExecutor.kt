package com.example.trafficgenerator.scripts

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.trafficgenerator.dto.GetTasksResponseDTO
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executor

/*
    Task implementation devs - insert your task functions here as follows:
    1. Add your function (GetTasksResponseDTO) -> (GetTasksResponseDTO) as a private function to the AsyncTaskExecutor class
    2. Add an entry to the taskFunctionMap with GetTasksResponseDTO.taskType as they key, which is the string task type for your task, and the handler function as the value
 */


class AsyncTaskExecutor(private val executor: Executor) {

    private val timingDateFormat = SimpleDateFormat("yyyy/MM/dd_HH:mm:ss", Locale.US)

    private val taskFunctionMap = mapOf<String, (GetTasksResponseDTO) -> (GetTasksResponseDTO)>(
        "sample" to ::sampleTaskHandler,
        "ftp" to ::ftpTaskHandler,
        "http" to ::httpTaskHandler,
        "video" to ::streamingTaskHandler
    )

    // Executor takes care of putting the task in an execution queue, even if a task is already running
    @RequiresApi(Build.VERSION_CODES.N)
    fun addTaskToExecutionQueue(task: GetTasksResponseDTO, callback: (GetTasksResponseDTO) -> (Unit)) {
        executor.execute {
            val response = taskFunctionMap.getOrDefault(task.taskType) { task ->
                throw NoSuchMethodException(task.taskType)
            }.invoke(task)
            callback(response)
        }
    }

    /*Templates for implementations & sample task-executing function
        * The steps to execute a task are as follows:
        * 1. Get current time to later insert into the DTO
        * 2. Execute the task and save the result ("success"/"fail")
        * 3. Copy the original task, and change ONLY the status, orderStart and orderEnd fields in the copy
        * 4. Return the newly copied task
    */
    private fun sampleTaskHandler(task: GetTasksResponseDTO): GetTasksResponseDTO {
        val startDate = timingDateFormat.format(Date())
        Thread.sleep(5000) // <- insert your implementation here
        return task.copy(
            status = "fail",
            orderStart = startDate,
            orderEnd = timingDateFormat.format(Date())
        )
    }

    private fun ftpTaskHandler(task: GetTasksResponseDTO): GetTasksResponseDTO {
        val startDate = timingDateFormat.format(Date())
        // insert your implementation here
        return task.copy(
            status = "fail",
            orderStart = startDate,
            orderEnd = timingDateFormat.format(Date())
        )
    }

    private fun httpTaskHandler(task: GetTasksResponseDTO): GetTasksResponseDTO {
        val startDate = timingDateFormat.format(Date())
        // insert your implementation here
        return task.copy(
            status = "fail",
            orderStart = startDate,
            orderEnd = timingDateFormat.format(Date())
        )
    }

    private fun streamingTaskHandler(task: GetTasksResponseDTO): GetTasksResponseDTO {
        val startDate = timingDateFormat.format(Date())
        // insert your implementation here
        return task.copy(
            status = "fail",
            orderStart = startDate,
            orderEnd = timingDateFormat.format(Date())
        )
    }
}