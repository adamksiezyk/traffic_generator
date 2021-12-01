package com.example.trafficgenerator.scripts

import android.util.Log
import it.sauronsoftware.ftp4j.FTPClient
import java.io.File

var DEBUG = true

const val Logger: String = "FtpClient"
class FtpClient(
    private val host: String = "10.0.2.2",
    private val port: Int = 21,
    private val userName: String = "fate",
    private val password: String = "fate"
) {
/*
*   This class is an Adapter to the sauronsoftware's FtpClient Implementation made to suit our needs.
*   For it to work - you need a server with a static IP (host) and WORKING FTP service.
*
*   To configure the client just parse the:
*       - `host`    : String
*       - `port`    : Int
*       - `userName`: String
*       - `password`: String
*   arguments in the constructor.
*
*   You also can set the remote's starting directory with `remoteCwd`: String variable.
*
*   All logging can be seen with logcat @ info level when using 'FtpClient` as filter.
* */

    private val mFtpClient = FTPClient()
    private val remoteCwd = "/home/fate/Downloads/"

    fun establishConnection() {
    /*
    * This function tries to:
    *   - Establish a connection with a given host on desired port and use credentials to log-in.
    *   - Change the transfer type to binary and the current remote dir to the specified one.
    * */
        try {
            log("Trying to connect to $host:$port...")
            mFtpClient.connect(host, port)
            log("Successfully connected!")
            log("Trying to log-in with $userName user...")
            mFtpClient.login(userName, password)
            log("Successfully logged in!")
            mFtpClient.type = FTPClient.TYPE_BINARY
            log("Changed current remote directory to: $remoteCwd")
            mFtpClient.changeDirectory(remoteCwd)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun upload(file: File) {
        /*
        * This function:
        *   - Uploads files from the device to the remote location
        *   - Measures time the transfer takes
        * */
        val start = System.currentTimeMillis()
        log("Uploading\t$file\tto\t$remoteCwd\t...")
        mFtpClient.upload(file)
        log("Done\t[${(System.currentTimeMillis() - start) / 1000.0}s]")
        log("Uploaded\t$remoteCwd${file.name} successfully.")
    }

    fun download(remoteFileName: String, file: File) {
        /*
        * This function:
        *   - Downloads files from the remote location to the device
        *   - Measures time the transfer takes
        * */
        val start = System.currentTimeMillis()
        log("Downloading\t$remoteCwd$remoteFileName\tto\t$file\t...")
        mFtpClient.download(remoteFileName, file)
        log("Done\t[${(System.currentTimeMillis() - start) / 1000.0}s]")
        log("Downloaded\t$file successfully.")
    }
}

fun log(message: String){
    /*
    * Centralized logging configuration manager
    * */
    if (DEBUG) {
        Log.i(Logger, message)
    }
}

fun main(){
    // ---- EXAMPLE USAGE ---- //

    val ftp = FtpClient()
    ftp.establishConnection()

    val fileName = "color.zip"
    val applicationDirPath = "/data/data/com.example.trafficgenerator/"
    ftp.download(fileName, File("${applicationDirPath}${fileName}"))
    ftp.upload(File("${applicationDirPath}${fileName}"))
}