package com.samdev.heartbeat.receivers

import android.util.Log
import com.samdev.heartbeat.callbacks.TransactionListener
import com.neovisionaries.ws.client.*
import java.util.*

class SocketListener : WebSocketListener {
    private val TAG = "TAG"
    var trnListener: TransactionListener? = null

    @Throws(Exception::class)
    override fun onStateChanged(websocket: WebSocket, newState: WebSocketState) {
        Log.e("State change: ", newState.name)
    }

    @Throws(Exception::class)
    override fun onConnected(websocket: WebSocket, headers: Map<String, List<String>>) {
        Log.e("socket connected", "")
        if (trnListener != null) {
            trnListener!!.onConnected()
        } else {
            Log.e("Listener is null", "")
        }
    }

    @Throws(Exception::class)
    override fun onConnectError(websocket: WebSocket, cause: WebSocketException) {
        // called when connection to the API is asynchronous.
        Log.e(TAG, "onConnect Error: ${cause.message}")
    }

    @Throws(Exception::class)
    override fun onDisconnected(websocket: WebSocket, serverCloseFrame: WebSocketFrame, clientCloseFrame: WebSocketFrame, closedByServer: Boolean) {
        Log.e("TAG", "Disconnected. By Server? $closedByServer")

        // reestablish connection
        // ArkeApplication.getInstance().reConnectWebsocket();
        if (trnListener != null) {
            trnListener!!.onDisconnected()
        } else {
            Log.e("TAG", "Listener is null")
        }
    }

    @Throws(Exception::class)
    override fun onFrame(websocket: WebSocket, frame: WebSocketFrame) {
        Log.e("onFrame: ", Arrays.toString(frame.payload))
    }

    @Throws(Exception::class)
    override fun onContinuationFrame(websocket: WebSocket, frame: WebSocketFrame) {
    }

    @Throws(Exception::class)
    override fun onTextFrame(websocket: WebSocket, frame: WebSocketFrame) {
        Log.e("Text frame: ", Arrays.toString(frame.payload))
    }

    @Throws(Exception::class)
    override fun onBinaryFrame(websocket: WebSocket, frame: WebSocketFrame) {
        Log.e("on Binary frame: ", Arrays.toString(frame.payload))
    }

    @Throws(Exception::class)
    override fun onCloseFrame(websocket: WebSocket, frame: WebSocketFrame) {
    }

    @Throws(Exception::class)
    override fun onPingFrame(websocket: WebSocket, frame: WebSocketFrame) {
        Log.e("Ping fame: ", frame.payloadText)
    }

    @Throws(Exception::class)
    override fun onPongFrame(websocket: WebSocket, frame: WebSocketFrame) {
        Log.e("Pong frame: ", frame.payloadText)
    }

    @Throws(Exception::class)
    override fun onTextMessage(websocket: WebSocket, text: String) {
        Log.e("TAG", "Text message received: $text")
    }

    @Throws(Exception::class)
    override fun onTextMessage(websocket: WebSocket, data: ByteArray) {
        Log.e("TAG", "Byte Text message received: " + Arrays.toString(data))
    }

    @Throws(Exception::class)
    override fun onBinaryMessage(websocket: WebSocket, binary: ByteArray) {
        var s = String(binary)
        s = s.substring(1)
        Log.e("TAG", "Binary message receive-: $s")
        if (trnListener != null) {
            trnListener?.onTextReceived(s)
        } else {
            Log.e("TAG", "Listener is null")
        }
    }

    @Throws(Exception::class)
    override fun onSendingFrame(websocket: WebSocket, frame: WebSocketFrame) {
    }

    @Throws(Exception::class)
    override fun onFrameSent(websocket: WebSocket, frame: WebSocketFrame) {
        var s = frame.payloadText
        s = s.substring(1)
        Log.e("Frame sent: ", s)
        if (trnListener != null) {
            trnListener?.onMessageSent()
        } else {
            Log.e("TAG", "Listener is null")
        }
    }

    @Throws(Exception::class)
    override fun onFrameUnsent(websocket: WebSocket, frame: WebSocketFrame) {
        Log.e("fame unsent: ", frame.payloadText)
    }

    @Throws(Exception::class)
    override fun onThreadCreated(websocket: WebSocket, threadType: ThreadType, thread: Thread) {
        Log.e("TAG", "thread  created")
    }

    @Throws(Exception::class)
    override fun onThreadStarted(websocket: WebSocket, threadType: ThreadType, thread: Thread) {
        Log.e("TAG", "thread  started")
    }

    @Throws(Exception::class)
    override fun onThreadStopping(websocket: WebSocket, threadType: ThreadType, thread: Thread) {
    }

    @Throws(Exception::class)
    override fun onError(websocket: WebSocket, cause: WebSocketException) {
        Log.e("TAG onError ", cause.localizedMessage)
    }

    @Throws(Exception::class)
    override fun onFrameError(websocket: WebSocket, cause: WebSocketException, frame: WebSocketFrame) {
        Log.e("frame error: ", cause.localizedMessage)
    }

    @Throws(Exception::class)
    override fun onMessageError(websocket: WebSocket, cause: WebSocketException, frames: List<WebSocketFrame>) {
        Log.e("message error: ", cause.localizedMessage)
    }

    @Throws(Exception::class)
    override fun onMessageDecompressionError(websocket: WebSocket, cause: WebSocketException, compressed: ByteArray) {
    }

    @Throws(Exception::class)
    override fun onTextMessageError(websocket: WebSocket, cause: WebSocketException, data: ByteArray) {
        Log.e("text message error: ", cause.localizedMessage)
    }

    @Throws(Exception::class)
    override fun onSendError(websocket: WebSocket, cause: WebSocketException, frame: WebSocketFrame) {
        Log.e("send error: ", " ${cause.localizedMessage} frame: ${frame.payloadText}")
    }

    @Throws(Exception::class)
    override fun onUnexpectedError(websocket: WebSocket, cause: WebSocketException) {
    }

    @Throws(Exception::class)
    override fun handleCallbackError(websocket: WebSocket, cause: Throwable) {
    }

    @Throws(Exception::class)
    override fun onSendingHandshake(websocket: WebSocket, requestLine: String, headers: List<Array<String>>) {
        Log.e("sending handshake: ", requestLine)
    }

    fun setTransactionListener(trnListener: TransactionListener?) {
        this.trnListener = trnListener
    }
}