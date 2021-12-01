package com.example.trafficgenerator.scripts

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.trafficgenerator.dto.GetTasksResponseDTO
import java.io.File
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

    private fun parseFileURL(fileURL: String): List<String> {

        /*
        * fileUrl format: `ftp://<HOST>/<PATH>/<TO>/<FILE>.<FILE_EXTENSION>`
        *
        * Output: a String array containing (<HOST>, <PATH>/<TO>/FILE>, <FILE>.<FILE_EXTENSION>)
        * */

        val host        : String
        val remoteDir   : String
        val remoteFile  : String

        // Get rid of `ftp://` suffix and split the remaining elements separated by single slashes to a mutable string
        val temporaryList: MutableList<String> = fileURL.split("//")[1].split("/") as MutableList<String>

        host = temporaryList.removeFirst()

        remoteFile = temporaryList.removeLast()

        remoteDir = temporaryList.joinToString("/", prefix = "/", postfix = "/")

        return listOf(host, remoteDir, remoteFile)
    }

    private fun ftpTaskHandler(task: GetTasksResponseDTO): GetTasksResponseDTO {
        val startDate = timingDateFormat.format(Date())

        // --- FTP --- //

        try {
            // Expecting fileURL to be of format - ftp://<HOST>/<PATH>/<TO>/<FILE>.<FILE_EXTENSION>
            val parsedFileURL: List<String> = parseFileURL(task.fileUrl)

            val host      = parsedFileURL[0]
            val remoteDir = parsedFileURL[1]
            val fileName  = parsedFileURL[2]

            val ftp = FtpClient(host = host, remoteCwd = remoteDir)
            ftp.establishConnection()

            val applicationDirPath =
                "/data/data/com.example.trafficgenerator/" // App's data directory

            // Download the `fileName` file from remote and upload it back
            ftp.download(fileName, File("${applicationDirPath}${fileName}"))
            ftp.upload(File("${applicationDirPath}${fileName}"))

            // Finished successfully - set status to `pass`
            return task.copy(
                status = "pass",
                orderStart = startDate,
                orderEnd = timingDateFormat.format(Date())
            )

        } catch (e: Exception) {
            // If anything failed - set status to `fail`
            return task.copy(
                status = "fail",
                orderStart = startDate,
                orderEnd = timingDateFormat.format(Date())
            )
        }
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