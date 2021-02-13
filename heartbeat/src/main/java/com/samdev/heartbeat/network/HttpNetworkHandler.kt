package com.samdev.heartbeat.network

import android.util.Log

class HttpNetworkHandler : NetworkHandler() {

    override fun sendMessage(payload: String) {
        Log.e("HTTP_HANDLER", "received payload")
    }
}