package com.expresspay.heartbeat.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Heartbeat implements Serializable {

    public enum Connectivity{
        WIFI,
        MOBILE_DATA,
        UNKNOWN
    }

    @SerializedName("ip")
    public String ip_address = "";

    @SerializedName("gps")
    public String gps_address = "";

    @SerializedName("net")
    public String network_type = "";

    @SerializedName("rssi")
    public String network_signal_strength = "";

    @SerializedName("batt")
    public String battery_life = "";

    @SerializedName("ver")
    public String heartbeat_version = "";

    @androidx.annotation.NonNull
    @Override
    public String toString() {
        return "Heartbeat{" +
                ", ip_address='" + ip_address + '\'' +
                ", gps_address='" + gps_address + '\'' +
                ", network_type='" + network_type + '\'' +
                ", network_signal_strength='" + network_signal_strength + '\'' +
                ", battery_life='" + battery_life + '\'' +
                ", software_version='" + heartbeat_version + '\'' +
                '}';
    }
}