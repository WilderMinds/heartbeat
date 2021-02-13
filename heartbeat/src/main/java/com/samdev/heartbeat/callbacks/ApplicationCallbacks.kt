package com.samdev.heartbeat.callbacks

interface ApplicationCallbacks {
    // handle network errors
    fun onNetworkError(throwable: Throwable)
    fun onNetworkSuccess(payload: Any?)

    /**
     * If location is required but turned off, provide callback
     * to allow custom behavior
     */
    fun onLocationDisabled()
    fun onLocationEnabled()

    fun onHeartbeatPayloadReceived(payload: String?)
}