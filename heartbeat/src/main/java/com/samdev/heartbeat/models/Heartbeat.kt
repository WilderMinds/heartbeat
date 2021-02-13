package com.samdev.heartbeat.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Heartbeat (

        /**
         * Sends device IP address
         */
        @SerializedName("ip")
        var ip_address: String = "",


        /**
         * GPS coordinates of the device
         */
        @SerializedName("gps")
        var gps_address: String = "",


        /**
         * Send the current network type.
         * [WIFI, MOBILE_DATA or UNKNOWN]
         */
        @SerializedName("net")
        var network_type: String = "",


        /**
         * Network signal strength.
         * Sends signal strength if `network_type` = WIFI or MOBILE_DATA
         * Sends "0 dbm" if `network_type` = UNKNOWN
         */
        @SerializedName("rssi")
        var network_signal_strength: String = "",


        /**
         * Battery life send as fraction (eg: 0.69 = 69%)
         */
        @SerializedName("batt")
        var battery_life: String = "",


        /**
         * Heartbeat library version
         */
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