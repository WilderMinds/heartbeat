package com.samdev.heartbeat.network

import android.util.Log
import com.google.gson.JsonParser
import com.samdev.heartbeat.HeartbeatController
import com.samdev.heartbeat.callbacks.ApplicationCallbacks
import com.samdev.heartbeat.network.retrofit.ApiService
import com.samdev.heartbeat.network.retrofit.RetrofitClient
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class HttpNetworkHandler(private val applicationCallbacks: ApplicationCallbacks) : NetworkHandler() {

    private val apiService: ApiService = RetrofitClient.apiService

    override fun sendMessage(payload: String) {
        Log.e("HTTP_HANDLER", "received payload")

        GlobalScope.launch {

            try {
                // convert string payload into JsonObject
                val jsonObject = JsonParser.parseString(payload).asJsonObject

                // send via api call
                val response = apiService.uploadHeartbeat(jsonObject)

                // persist
                HeartbeatController.instance.saveLastPayload(payload)

                // pass the response via the callback
                applicationCallbacks.onNetworkSuccess(response)
            } catch (e: Exception) {
                e.printStackTrace()

                // send any errors received
                // errors may include responses with status codes != [200 .. 202]
                applicationCallbacks.onNetworkError(e)
            }
        }
    }
}