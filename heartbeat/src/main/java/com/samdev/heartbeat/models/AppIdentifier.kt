package com.samdev.heartbeat.models

import com.google.gson.annotations.SerializedName
import com.samdev.heartbeat.HeartbeatController
import java.io.Serializable

data class AppIdentifier(

        /**
         * Device identifier.
         * Has a default value if no value is assigned.
         */
        @SerializedName("device-id")
        var deviceId: String = HeartbeatController.instance.randomString(16),


        /**
         * Application ID
         */
        @SerializedName("appid")
        var appId: String = "",


        /**
         * App name
         */
        @SerializedName("appname")
        var appName: String = "",


        /**
         * App version
         */
        @SerializedName("appver")
        var appVer: String = ""

) : Serializable {
    override fun toString(): String {
        return "Payload{" +
                ", deviceId='" + deviceId + '\'' +
                ", appId='" + appId + '\'' +
                ", appName='" + appName + '\'' +
                ", appVer='" + appVer + '\'' +
                '}'
    }
}