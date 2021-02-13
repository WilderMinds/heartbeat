package com.samdev.heartbeat.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Heartbeat (

    @SerializedName("ip")
    var ip_address: String = "",

    @SerializedName("gps")
    var gps_address: String = "",

    @SerializedName("net")
    var network_type: String = "",

    @SerializedName("rssi")
    var network_signal_strength: String = "",

    @SerializedName("batt")
    var battery_life: String = "",

    @SerializedName("ver")
    var heartbeat_version: String = ""

) : Serializable {
    override fun toString(): String {
        return "Heartbeat{" +
                ", ip_address='" + ip_address + '\'' +
                ", gps_address='" + gps_address + '\'' +
                ", network_type='" + network_type + '\'' +
                ", network_signal_strength='" + network_signal_strength + '\'' +
                ", battery_life='" + battery_life + '\'' +
                ", software_version='" + heartbeat_version + '\'' +
                '}'
    }
}

enum class Connectivity {
    WIFI, MOBILE_DATA, UNKNOWN
}