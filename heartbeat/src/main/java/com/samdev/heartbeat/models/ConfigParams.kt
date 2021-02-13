package com.samdev.heartbeat.models

import com.google.gson.annotations.SerializedName

data class ConfigParams (
    // config details
    var socketUrl: String = "",

    @SerializedName("device-id")
    var deviceId: String = "",

    // trigger interval
    var triggerIntervalMillis: Int = 0,

    // persist socket connection
    var isPersistSocketConnection: Boolean = false,

    // app details
    @SerializedName("version")
    var apiVersion: String = "",

    @SerializedName("appid")
    var appId: String = "",

    @SerializedName("appname")
    var appName: String = "",

    @SerializedName("appver")
    var appVer: String = ""
)