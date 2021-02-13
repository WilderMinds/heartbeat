package com.samdev.heartbeat.callbacks

interface TransactionListener {
    fun onTextReceived(text: String?)
    fun onDisconnected()
    fun onMessageSent()
    fun onConnected()
}