package com.samdev.heartbeat.models

import com.google.gson.annotations.SerializedName
import com.samdev.heartbeat.HeartbeatController
import java.util.*

data class ConfigParams (

        /**
         * Either websocket url or API endpoint.
         */
        var networkUrl: String = "",

        /**
         * Set to true only if you want to use the webSocket implementation regardless
         * of your `networkUrl` scheme
         */
        var forceSendViaWebSocket: Boolean = false,


        /**
         * Set to true only if you want to use the API implementation regardless
         * of your `networkUrl` scheme
         */
        var forceSendViaApiCall: Boolean = false,


        /**
         * Interval to used when sending periodic updates
         */
        var triggerIntervalMillis: Int = 0,


        /**
         * When set to true, service will keep socket connection open.
         * Will endlessly attempt to reconnect if connection errors occur or
         * socket is closed by the server.
         *
         * Note that setting `isPersistSocketConnection` to true will have no effect
         * if the heartbeats are sent via Api call.
         */
        var isPersistSocketConnection: Boolean = false,


        /**
         * Determines whether heartbeat payload should include device location data
         */
        var trackDeviceLocation: Boolean = false,

        /**
        * Fields that will identify the device and app instance sending the payload
         */
        var addIdentifier: AppIdentifier = AppIdentifier()
)