package com.samdev.heartbeat.callbacks

interface OnBroadcastDataReceived {
    fun onSignalStrengthChanged(value: Int)
    fun onBatteryLevelChanged(level: Int)
}