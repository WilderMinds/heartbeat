package com.expresspay.heartbeat.callbacks;

public interface TransactionListener {
    void onTextReceived(String text);
    void onDisconnected();
    void onMessageSent();
    void onConnected();
}
