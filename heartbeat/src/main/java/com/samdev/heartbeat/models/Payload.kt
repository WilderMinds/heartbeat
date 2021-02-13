package com.samdev.heartbeat.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Payload(
    @SerializedName("device-id")
    var deviceId: String = "",

    @SerializedName("version")
    var apiVersion: String = "",

    @SerializedName("appid")
    var appId: String = "",

    @SerializedName("appname")
    var appName: String = "",

    @SerializedName("appver")
    var appVer: String = ""

) : Serializable {
    override fun toString(): String {
        return "Payload{" +
                "version='" + apiVersion + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", appId='" + appId + '\'' +
                ", appName='" + appName + '\'' +
                ", appVer='" + appVer + '\'' +
                '}'
    }
}