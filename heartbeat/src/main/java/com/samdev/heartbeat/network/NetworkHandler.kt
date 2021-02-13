package com.samdev.heartbeat.network

import android.util.Log

abstract class NetworkHandler {

    open fun sendMessage(payload: String) {
        Log.e("NETWORK_HANDLER", "received payload")
    }
}