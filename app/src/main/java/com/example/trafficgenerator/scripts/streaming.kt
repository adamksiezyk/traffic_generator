package com.example.trafficgenerator.scripts

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri

class streaming (
    private val url: String
){
    var mediaPlayer: MediaPlayer? = null
    //quick preload start and release, have issues with buffering media in the background
    //this fun need context to run media player soo I am passing it through GetTaskRespond
    fun playSound(context: Context){
        mediaPlayer = MediaPlayer.create(context, Uri.parse(url))
        mediaPlayer?.start()
        mediaPlayer?.release() //comment this line and u will hear some pleasure music

    }
}