package com.samdev.heartbeat.network

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.samdev.heartbeat.HeartbeatController
import com.samdev.heartbeat.callbacks.TransactionListener
import com.neovisionaries.ws.client.WebSocket
import com.neovisionaries.ws.client.WebSocketState
import java.io.ByteArrayOutputStream

class WsNetworkHandler: NetworkHandler() {

    private lateinit var webSocket: WebSocket
    private val config = HeartbeatController.instance.configParams
    private var isAwaitingReconnection = false
    private var payload = ""

    init {
        initWebSocket()
    }

    private fun initWebSocket() {
        webSocket = HeartbeatController.instance.webSocket
        HeartbeatController.instance.socketListener
                .setTransactionListener(object : TransactionListener {
                    override fun onTextReceived(text: String?) {}
                    override fun onDisconnected() {
                        if (config.isPersistSocketConnection) {
                            if (webSocket.state == WebSocketState.CREATED) {
                                webSocket.connectAsynchronously()
                            } else {
                                webSocket = HeartbeatController.instance.reconnectWebSocket()!!
                            }
                        }
                    }

                    override fun onMessageSent() {}
                    override fun onConnected() {
                        Log.e("TAG", "Internal connected callback triggered, awaitingConnectionToSendHeartbeat => $isAwaitingReconnection")
                        // if reconnect has been triggered while attempting to send a message
                        if (isAwaitingReconnection) {
                            isAwaitingReconnection = false
                            val handler = Handler(Looper.getMainLooper())
                            val runnable = Runnable {
                                Log.e("TAG", "2nd attempt to send message after socket reconnection")
                                ensureOpenConnectionAndSend()
                            }

                            // allow 5 seconds for attempting a retry
                            handler.postDelayed(runnable, 3000)
                        }
                    }
                })
    }


    fun ensureOpenConnectionAndSend() {
        Log.e("WS_HANDLER", "received payload")
        if (webSocket.isOpen) {
            send()
        } else {
            isAwaitingReconnection = true
            if (webSocket.state == WebSocketState.CREATED) {
                webSocket.connectAsynchronously()
                Log.e("TAG", "websocket not open but created, connecting asynchronously")
                return
            }

            // reconnect
            webSocket = HeartbeatController.instance.reconnectWebSocket()!!
            Log.e("TAG", "websocket closed, initializing reconnect")
        }
    }

    private fun buildHeartbeatBinary(): ByteArray? {
        return try {
            // determine whether one-off connection or subscription
            val buffer1 = ByteArray(1)
            buffer1[0] = 0 // change to 1 if more messages will coming through
            //buffer1[1] = (subscribe ? (byte) '1' : (byte) '0');

            if (payload.isEmpty()) {
                throw Exception("payload is empty")
            }
            HeartbeatController.instance.saveLastPayload(payload)

            // get message in bytes
            val buffer2 = payload.toByteArray()

            // object to compile message
            val byteArrayOutputStream = ByteArrayOutputStream()
            byteArrayOutputStream.write(buffer1) // defines mode of transfer
            byteArrayOutputStream.write(buffer2) // defines the message to be sent
            byteArrayOutputStream.toByteArray()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun send() {
        Log.e("Tag", "attempting send message")
        try {
            val message = buildHeartbeatBinary()
            if (message != null) {
                Log.e("TAG", String(message))
                webSocket.sendBinary(message)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    override fun sendMessage(payload: String) {
        this.payload = payload
        ensureOpenConnectionAndSend()
    }


}