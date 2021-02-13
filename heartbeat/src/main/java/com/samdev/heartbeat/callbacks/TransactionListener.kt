package com.samdev.heartbeat.callbacks

interface TransactionListener {
    fun onConnected()
    fun onDisconnected()
    fun onError(cause: Exception)
    fun onTextReceived(text: String?)
}