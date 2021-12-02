package com.example.trafficgenerator.scripts

import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result


//var DEBUG = true
//const val Logger: String = "HttpRequest"

class httprequest (
    private val url: String
){

    fun httpRequest(){
        val start = System.currentTimeMillis()
        var data = ""
        url.httpGet().responseString { request, response, result ->
            when (result) {
                is Result.Failure -> {
                    println("error")
                }
                is Result.Success -> {
                    data = result.get()
                    println(data)
                    println("Done\t[${(System.currentTimeMillis() - start) / 1000.0}s]")
                }
            }
        }
    }
}
