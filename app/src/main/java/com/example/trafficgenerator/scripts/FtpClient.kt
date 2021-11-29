package com.example.trafficgenerator.scripts

import android.util.Log
import it.sauronsoftware.ftp4j.FTPClient
import com.example.trafficgenerator.dto.GetTasksResponseDTO
import java.io.File

var DEBUG = true

const val Logger: String = "FtpClient"
class FtpClient(
    private val host: String, private val userName: String, private val password: String, private val port: Int = 22
) {

    private val mFtpClient = FTPClient()
    private var remoteCwd = "/home/fate/Downloads/"
    private var connectionEstablished = false

    var loggedIn = false

    fun establishConnection(){
        try {
            Log.e(Logger, "Trying to connect...")
            mFtpClient.connect(host, port)
            connectionEstablished = true
            Log.e(Logger, "Trying to log in...")
            mFtpClient.login(userName, password)
            loggedIn = true
            Log.e(Logger, "Changing client type to bin...")
            mFtpClient.type = FTPClient.TYPE_BINARY
            mFtpClient.isPassive = false
            Log.e(Logger, "Changing remote directory...")
            mFtpClient.changeDirectory(remoteCwd)
            Log.e(Logger, "Changed remote dir to: $remoteCwd")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun upload(file: File) {
        val start = System.currentTimeMillis()
        log("Uploading...", nextLine = false)
        mFtpClient.upload(file)
        log("Done\t[${(System.currentTimeMillis() - start) / 1000.0}s]")
        log("Uploaded\t$file\tto  \t$remoteCwd${file.name}")
    }

    fun download(remoteFileName: String, file: File) {
        val start = System.currentTimeMillis()
        log("Downloading...", nextLine = false)
        mFtpClient.download(remoteFileName, file)
        log("Done\t[${(System.currentTimeMillis() - start) / 1000.0}s]")
        log("Downloaded\t$file\tfrom\t$remoteCwd$remoteFileName")
    }
}

fun log(message: String, nextLine: Boolean = true) {
    if (DEBUG) {
        if (nextLine)
            println(message)
        else
            print(message)
    }
}

fun main() {
//    TODO: GetTasksResponseDTO integration

    val ftp = FtpClient("10.0.2.2","fate", "fate")
    val fileName = "install_server-6a9a542f"
    val appDataPath = "/data/data/com.example.ftp5g/"
    ftp.upload(File("${appDataPath}code_cache/$fileName"))
    ftp.download(fileName, File("${appDataPath}${fileName}"))
}