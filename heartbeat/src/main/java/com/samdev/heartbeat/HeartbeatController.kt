package com.samdev.heartbeat

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.samdev.heartbeat.models.ConfigParams
import com.samdev.heartbeat.receivers.SocketListener
import com.neovisionaries.ws.client.WebSocket
import com.neovisionaries.ws.client.WebSocketFactory
import com.neovisionaries.ws.client.WebSocketState
import com.samdev.heartbeat.callbacks.ApplicationCallbacks
import java.util.*

/**
 * This class must contain all the data needed for the heartbeat service to run
 */
class HeartbeatController {

    companion object {
        val instance: HeartbeatController = HeartbeatController()
        const val LAST_PAYLOAD = "LAST_PAYLOAD"
        const val RANGE: String = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ/()+?@$&!%*-"
    }

    val socketListener: SocketListener = SocketListener()
    private var preferences: SharedPreferences? = null

    var heartbeatConfig: HeartbeatConfig? = null
    var webSocket: WebSocket? = null
    lateinit var configParams: ConfigParams
    val additionalParams: MutableMap<String, String?> = HashMap()


    /**
     * Method to generate random string
     */
    fun randomString(len: Int): String {
        val sb = StringBuilder(len)
        for (i in 0 until len) sb.append(RANGE[Random().nextInt(RANGE.length)])
        return sb.toString()
    }


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
        setPreferencesFor(LAST_PAYLOAD, payload)
    }


    val lastPayload: String
        get() = getPreferencesFor(LAST_PAYLOAD, "")


    fun initWebSocket(): WebSocket? {
        try {
            //WebSocketFactory factory = new WebSocketFactory().setConnectionTimeout(15000);
            webSocket = WebSocketFactory().createSocket(configParams.networkUrl)
            webSocket?.let {
                it.addListener(socketListener)
                it.addProtocol("WSNetMQ")

                if (!it.isOpen) {
                    if (it.state == WebSocketState.CREATED) {
                        it.connectAsynchronously()
                    } else {
                        reconnectWebSocket()
                    }
                }
            }
            return webSocket
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }


    /**
     * Reconnect and assign to existing websocket instance
     */
    fun reconnectWebSocket(): WebSocket? {
        return try {
            webSocket = webSocket?.recreate()?.connectAsynchronously()
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
     *
     * NB: Since we are working with hashmaps, we can use this same
     * method to override already existing keys
     */
    fun addOrUpdateAdditionalParams(key: String, value: String) {
        additionalParams[key] = value
    }

    @Throws(Exception::class)
    fun startService(context: Context?, applicationCallbacks: ApplicationCallbacks?) {
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
        checkNotNull(applicationCallbacks) { "LocationSettingsListener is null" }

        // init webSocket
        // initWebSocket()
        heartbeatConfig = HeartbeatConfig(context, applicationCallbacks)
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
        heartbeatConfig!!.sendMessageHandler()
    }
}