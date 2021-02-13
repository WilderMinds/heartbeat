package com.expresspay.heartbeat.receivers;

import android.util.Log;

import com.expresspay.heartbeat.callbacks.TransactionListener;
import com.neovisionaries.ws.client.ThreadType;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFrame;
import com.neovisionaries.ws.client.WebSocketListener;
import com.neovisionaries.ws.client.WebSocketState;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class SocketListener implements WebSocketListener {
    public TransactionListener trnListener;

    @Override
    public void onStateChanged(WebSocket websocket, WebSocketState newState) throws Exception {
        Log.e("State change: %s", newState.name());
    }

    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
        Log.e("socket connected", "");
        if (trnListener != null) {
            trnListener.onConnected();
        } else {
            Log.e("Listener is null", "");
        }
    }

    @Override
    public void onConnectError(WebSocket websocket, WebSocketException cause) throws Exception {
        // called when connection to the API is asynchronous.
        Log.e("onConnect Error: %s",  cause.getLocalizedMessage());
    }

    @Override
    public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {
        Log.e("TAG", "Disconnected. By Server? %s" + String.valueOf(closedByServer));

        // reestablish connection
        // ArkeApplication.getInstance().reConnectWebsocket();
        if (trnListener != null) {
            trnListener.onDisconnected();
        } else {
            Log.e("TAG", "Listener is null");
        }
    }

    @Override
    public void onFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
        Log.e("onFrame: %s" , Arrays.toString(frame.getPayload()));
    }

    @Override
    public void onContinuationFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {

    }

    @Override
    public void onTextFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
        Log.e("Text frame: %s" , Arrays.toString(frame.getPayload()));
    }

    @Override
    public void onBinaryFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
        Log.e("on Binary frame: %s" , Arrays.toString(frame.getPayload()));
    }

    @Override
    public void onCloseFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {

    }

    @Override
    public void onPingFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
        Log.e("Ping fame: %s", frame.getPayloadText());
    }

    @Override
    public void onPongFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
        Log.e("Pong frame: %s" , frame.getPayloadText());
    }

    @Override
    public void onTextMessage(WebSocket websocket, String text) throws Exception {
        Log.e("TAG", "Text message received: %s" + text);
    }

    @Override
    public void onTextMessage(WebSocket websocket, byte[] data) throws Exception {
        Log.e("TAG", "Byte Text message received: %s" + Arrays.toString(data));
    }

    @Override
    public void onBinaryMessage(WebSocket websocket, byte[] binary) throws Exception {
        String s = new String(binary);
        s = s.substring(1);
        Log.e("TAG", "Binary message receive-: %s" + s);
        if (trnListener != null) {
            trnListener.onTextReceived(s);
        } else {
            Log.e("TAG", "Listener is null");
        }
    }

    @Override
    public void onSendingFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {

    }

    @Override
    public void onFrameSent(WebSocket websocket, WebSocketFrame frame) throws Exception {
        String s = frame.getPayloadText();
        s = s.substring(1);
        Log.e("Frame sent: %s" , s);
        if (trnListener != null) {
            trnListener.onMessageSent();
        } else {
            Log.e("TAG","Listener is null");
        }
    }

    @Override
    public void onFrameUnsent(WebSocket websocket, WebSocketFrame frame) throws Exception {
        Log.e("fame unsent: %s", frame.getPayloadText());
    }

    @Override
    public void onThreadCreated(WebSocket websocket, ThreadType threadType, Thread thread) throws Exception {
        Log.e("TAG","thread  created");
    }

    @Override
    public void onThreadStarted(WebSocket websocket, ThreadType threadType, Thread thread) throws Exception {
        Log.e("TAG","thread  started");
    }

    @Override
    public void onThreadStopping(WebSocket websocket, ThreadType threadType, Thread thread) throws Exception {

    }

    @Override
    public void onError(WebSocket websocket, WebSocketException cause) throws Exception {
        Log.e("TAG onError",cause.getLocalizedMessage());
    }

    @Override
    public void onFrameError(WebSocket websocket, WebSocketException cause, WebSocketFrame frame) throws Exception {
        Log.e("frame error: %s",  cause.getLocalizedMessage());
    }

    @Override
    public void onMessageError(WebSocket websocket, WebSocketException cause, List<WebSocketFrame> frames) throws Exception {
        Log.e("message error: %s", cause.getLocalizedMessage());
    }

    @Override
    public void onMessageDecompressionError(WebSocket websocket, WebSocketException cause, byte[] compressed) throws Exception {

    }

    @Override
    public void onTextMessageError(WebSocket websocket, WebSocketException cause, byte[] data) throws Exception {
        Log.e("text message error: %s", cause.getLocalizedMessage());
    }

    @Override
    public void onSendError(WebSocket websocket, WebSocketException cause, WebSocketFrame frame) throws Exception {
        Log.e("send error: %s", "" + cause.getLocalizedMessage() + " frame: " + frame.getPayloadText());
    }

    @Override
    public void onUnexpectedError(WebSocket websocket, WebSocketException cause) throws Exception {

    }

    @Override
    public void handleCallbackError(WebSocket websocket, Throwable cause) throws Exception {

    }

    @Override
    public void onSendingHandshake(WebSocket websocket, String requestLine, List<String[]> headers) throws Exception {
        Log.e("sending handshake: %s",  requestLine);
    }

    public void setTransactionListener(TransactionListener trnListener) {
        this.trnListener = trnListener;
    }
}
