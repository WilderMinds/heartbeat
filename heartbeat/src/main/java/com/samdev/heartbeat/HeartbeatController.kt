package com.samdev.heartbeat

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.samdev.heartbeat.callbacks.LocationSettingsListener
import com.samdev.heartbeat.models.ConfigParams
import com.samdev.heartbeat.receivers.SocketListener
import com.neovisionaries.ws.client.WebSocket
import com.neovisionaries.ws.client.WebSocketFactory
import com.neovisionaries.ws.client.WebSocketState
import java.util.*

/**
 * This class must contain all the data needed for the heartbeat service to run
 */
class HeartbeatController {

    companion object {
        val instance: HeartbeatController = HeartbeatController()
    }

    val socketListener: SocketListener = SocketListener()
    private var preferences: SharedPreferences? = null

    var heartbeatConfig: HeartbeatConfig? = null
    lateinit var webSocket: WebSocket
    lateinit var configParams: ConfigParams
    val additionalParams: MutableMap<String, String?> = HashMap()


    fun initSharedPrefs(context: Context) {
        preferences = context.getSharedPreferences("heartbeat", Context.MODE_PRIVATE)
    }

    private fun setPreferencesFor(key: String, value: String) {
        val editor = preferences?.edit()
        editor?.putString(key, value)
        editor?.apply()
    }

    private fun getPreferencesFor(key: String, defaultValue: String): String {
        return preferences?.getString(key, defaultValue).orEmpty()
    }

    fun saveLastPayload(payload: String) {
        setPreferencesFor("LAST_PAYLOAD", payload)
    }

    val lastPayload: String
        get() = getPreferencesFor("LAST_PAYLOAD", "")

    private fun initWebSocket() {
        try {
            // init factory
            //WebSocketFactory factory = new WebSocketFactory().setConnectionTimeout(15000);
            webSocket = WebSocketFactory().createSocket(configParams.socketUrl)
            webSocket.addListener(socketListener)
            webSocket.addProtocol("WSNetMQ")

            if (!webSocket.isOpen) {
                if (webSocket.state == WebSocketState.CREATED) {
                    webSocket.connectAsynchronously()
                } else {
                    reconnectWebSocket()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun reconnectWebSocket(): WebSocket? {
        return try {
            webSocket = webSocket.recreate().connectAsynchronously()
            webSocket
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * This will reset the dataset. Use this when you want to replace all the additional parameters
     * @param dataSet
     */
    fun setAdditionalParams(dataSet: Map<String, String>?) {
        additionalParams.clear()
        additionalParams.putAll(dataSet!!)
    }

    /**
     * Add a key-value pair to the already existing parameters
     * @param key
     * @param value
     */
    fun addAdditionalParams(key: String, value: String) {
        additionalParams[key] = value
    }

    @Throws(Exception::class)
    fun startService(context: Context?, locationSettingsListener: LocationSettingsListener?) {
        Log.e("TAG", "init webSocket and start service")

        checkNotNull(context) {
            "Context provided is null"
        }

        if (!this::configParams.isInitialized) {
            throw IllegalStateException( """
                ConfigParams is null.
                Please set the ConfigParams to proceed
                """.trimIndent())
        }
        checkNotNull(locationSettingsListener) { "LocationSettingsListener is null" }

        // init webSocket
        initWebSocket()
        heartbeatConfig = HeartbeatConfig(context, locationSettingsListener)
        heartbeatConfig!!.startService()
    }

    fun destroyService() {
        try {
            heartbeatConfig!!.destroyService()
        } catch (ignore: Exception) {
        }
    }

    @Throws(Exception::class)
    fun sendImmediately() {
        heartbeatConfig!!.collectHeartbeatData()
        heartbeatConfig!!.sendMessageHandler()
    }
}