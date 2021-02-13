package com.expresspay.heartbeat.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Payload implements Serializable {

    @SerializedName("device-id")
    public String deviceId = "";

    @SerializedName("version")
    public String apiVersion = "";

    @SerializedName("appid")
    public String appId = "";

    @SerializedName("appname")
    public String appName = "";

    @SerializedName("appver")
    public String appVer = "";

    @Override
    public String toString() {
        return "Payload{" +
                "version='" + apiVersion + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", appId='" + appId + '\'' +
                ", appName='" + appName + '\'' +
                ", appVer='" + appVer + '\'' +
                '}';
    }
}
