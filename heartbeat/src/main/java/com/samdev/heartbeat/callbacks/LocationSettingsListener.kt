package com.samdev.heartbeat.callbacks

interface LocationSettingsListener {
    fun onLocationDisabled()
    fun onLocationEnabled()
}