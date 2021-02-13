package com.samdev.heartbeat.network

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.samdev.heartbeat.HeartbeatController
import com.samdev.heartbeat.callbacks.TransactionListener
import com.neovisionaries.ws.client.WebSocket
import com.neovisionaries.ws.client.WebSocketState
import com.samdev.heartbeat.callbacks.ApplicationCallbacks
import java.io.ByteArrayOutputStream

class WsNetworkHandler(private val applicationCallbacks: ApplicationCallbacks) : NetworkHandler() {

    private var webSocket: WebSocket? = null
    private val config = HeartbeatController.instance.configParams
    private var isAwaitingReconnection = false
    private var payload = ""

    init {
        initWebSocket()
    }

    private fun initWebSocket() {

        // create if null
        webSocket = HeartbeatController.instance.webSocket
        if (webSocket == null) {
            webSocket = HeartbeatController.instance.initWebSocket()
        }

        // attach listener
        HeartbeatController.instance.socketListener
                .setTransactionListener(object : TransactionListener {

                    // reconnect if disconnected
                    override fun onDisconnected() {
                        if (config.isPersistSocketConnection) {
                            if (webSocket?.state == WebSocketState.CREATED) {
                                webSocket?.connectAsynchronously()
                            } else {
                                webSocket = HeartbeatController.instance.reconnectWebSocket()!!
                            }
                        }
                    }

                    // check if a previous attempt has been made to send a payload
                    // if yes, send the pending heartbeat payload
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

                            // allow 3 seconds for attempting a retry
                            handler.postDelayed(runnable, 3000)
                        }
                    }

                    override fun onTextReceived(text: String?) {
                        applicationCallbacks.onNetworkSuccess(text)
                    }

                    override fun onError(cause: Exception) {
                        applicationCallbacks.onNetworkError(cause)
                    }
                })
    }


    /**
     * Proceed with sending payload if the websocket is open, else
     * set the `isAwaitingReconnection` flag to TRUE and initialize
     * a reconnect.
     *
     * Once the socket is connected again, the pending payload will be sent
     * because of the the `isAwaitingReconnection` flag
     */
    fun ensureOpenConnectionAndSend() {
        webSocket?.let {
            Log.e("WS_HANDLER", "received payload")
            if (it.isOpen) {
                send()
            } else {
                isAwaitingReconnection = true
                if (it.state == WebSocketState.CREATED) {
                    it.connectAsynchronously()
                    Log.e("TAG", "websocket not open but created, connecting asynchronously")
                    return
                }

                // reconnect with same instance
                webSocket = HeartbeatController.instance.reconnectWebSocket()!!
                Log.e("TAG", "websocket closed, initializing reconnect")
            }
        }
    }


    /**
     * Convert string payload into bytes to be sent via
     * the websocket
     */
    private fun buildHeartbeatBinary(): ByteArray? {
        return try {
            // determine whether one-off connection or subscription
            val buffer1 = ByteArray(1)
            buffer1[0] = 0 // change to 1 if more messages will coming through

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
        webSocket?.let {
            Log.e("Tag", "attempting send message")
            try {
                val message = buildHeartbeatBinary()
                if (message != null) {
                    Log.e("TAG", String(message))
                    it.sendBinary(message)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    override fun sendMessage(payload: String) {
        this.payload = payload
        ensureOpenConnectionAndSend()
    }


}