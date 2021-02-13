package com.expresspay.heartbeat.controller;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.expresspay.heartbeat.HeartbeatConfig;
import com.expresspay.heartbeat.callbacks.LocationSettingsListener;
import com.expresspay.heartbeat.models.ConfigParams;
import com.expresspay.heartbeat.receivers.SocketListener;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketState;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * This class must contain all the data needed for the heartbeat service to run
 */
public class HeartbeatController {

    private static HeartbeatController mInstance;
    private HeartbeatConfig heartbeatConfig;
    private WebSocket webSocket;
    private SocketListener socketListener;

    private ConfigParams configParams;
    private Map<String, String> additionalParams = new HashMap<>();

    private SharedPreferences preferences;

    public HeartbeatController() {
        socketListener = new SocketListener();
    }

    public void initSharedPrefs(Context context) {
        preferences = context.getSharedPreferences("heartbeat", Context.MODE_PRIVATE);
    }

    private void setPreferencesFor(String key, String value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    private String getPreferencesFor(String key, String defaultValue) {
        return preferences.getString(key, defaultValue);
    }

    public void saveLastPayload(String payload) {
        setPreferencesFor("LAST_PAYLOAD", payload);
    }

    public String getLastPayload() {
        return getPreferencesFor("LAST_PAYLOAD", "");
    }

    private void initWebSocket() {
        try {
            // init factory
            //WebSocketFactory factory = new WebSocketFactory().setConnectionTimeout(15000);
            webSocket = new WebSocketFactory().createSocket(configParams.getSocketUrl());
            webSocket.addListener(socketListener);
            webSocket.addProtocol("WSNetMQ");

            if (!webSocket.isOpen()) {
                if (webSocket.getState() == WebSocketState.CREATED) {
                    webSocket.connectAsynchronously();
                } else {
                    reconnectWebSocket();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static synchronized HeartbeatController getInstance() {

        if (mInstance == null) {
            mInstance = new HeartbeatController();
        }
        return mInstance;
    }

    public WebSocket getWebSocket() {
        return webSocket;
    }

    public SocketListener getSocketListener() {
        return socketListener;
    }

    public WebSocket reconnectWebSocket() {
        try {
            webSocket = webSocket.recreate().connectAsynchronously();
            return webSocket;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setConfigParams(ConfigParams configParams) {
        this.configParams = configParams;
    }

    /**
     * This will reset the dataset. Use this when you want to replace all the additional parameters
     * @param dataSet
     */
    public void setAdditionalParams(Map<String,String> dataSet) {
        additionalParams.clear();
        additionalParams.putAll(dataSet);
    }

    /**
     * Add a key-value pair to the already existing parameters
     * @param key
     * @param value
     */
    public void addAdditionalParams(String key, String value) {
        additionalParams.put(key, value);
    }

    public ConfigParams getConfigParams() {
        return configParams;
    }

    public Map<String, String> getAdditionalParams() {
        return additionalParams;
    }

    public HeartbeatConfig getHeartbeatConfig() {
        return heartbeatConfig;
    }

    public void startService(Context context, LocationSettingsListener locationSettingsListener) throws Exception {
        Log.e("TAG", "init webSocket and start service");

        if (configParams == null) {
            throw new IllegalStateException("ConfigParams is null." +
                    "\nUse HeartbeatController.getInstance().setConfigParams() to set config object");
        }

        if (locationSettingsListener == null) {
            throw new IllegalStateException("LocationSettingsListener is null");
        }

        // init webSocket
        initWebSocket();

        heartbeatConfig = new HeartbeatConfig(context, locationSettingsListener);
        heartbeatConfig.startService();
    }

    public void destroyService() {
        try{
            heartbeatConfig.destroyService();
        } catch (Exception ignore){}
    }

    public void sendImmediately() throws Exception{
        heartbeatConfig.collectHeartbeatData();
        heartbeatConfig.sendMessageHandler();
    }

}