package com.expresspay.heartbeat.models;

import android.util.Log;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ConfigParams implements Serializable {

    // config details
    private String socketUrl;

    @SerializedName("device-id")
    private String deviceId;

    // trigger interval
    private int triggerIntervalMillis;

    // persist socket connection
    private boolean persistSocketConnection = false;

    // app details
    @SerializedName("version")
    private String apiVersion;

    @SerializedName("appid")
    private String appId;

    @SerializedName("appname")
    private String appName;

    @SerializedName("appver")
    private String appVer;

    public ConfigParams() {
    }

    public String getSocketUrl() {
        return socketUrl;
    }

    public void setSocketUrl(String socketUrl) {
        this.socketUrl = socketUrl;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public int getTriggerIntervalMillis() {
        return triggerIntervalMillis;
    }

    public void setTriggerIntervalMillis(int triggerIntervalMillis) {
        Log.e("TAG", "set time millis = " + triggerIntervalMillis);
        this.triggerIntervalMillis = triggerIntervalMillis;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppVer() {
        return appVer;
    }

    public void setAppVer(String appVer) {
        this.appVer = appVer;
    }

    public boolean isPersistSocketConnection() {
        return persistSocketConnection;
    }

    public void setPersistSocketConnection(boolean persistSocketConnection) {
        this.persistSocketConnection = persistSocketConnection;
    }
}
