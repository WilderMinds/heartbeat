package com.samdev.heartbeat

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.Bundle
import android.provider.Settings
import android.provider.Settings.SettingNotFoundException
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import androidx.work.*
import com.samdev.heartbeat.callbacks.LocationSettingsListener
import com.samdev.heartbeat.callbacks.OnBroadcastDataReceived
import com.samdev.heartbeat.models.Connectivity
import com.samdev.heartbeat.models.Heartbeat
import com.samdev.heartbeat.models.Payload
import com.samdev.heartbeat.network.HttpNetworkHandler
import com.samdev.heartbeat.network.NetworkHandler
import com.samdev.heartbeat.network.WsNetworkHandler
import com.samdev.heartbeat.receivers.BatteryLevelBroadcastReceiver
import com.samdev.heartbeat.receivers.MyCellularStateListener
import com.samdev.heartbeat.service.AlarmReceiver
import com.samdev.heartbeat.service.HeartbeatWorker
import com.google.gson.GsonBuilder
import org.json.JSONObject
import java.net.Inet4Address
import java.net.NetworkInterface
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class HeartbeatConfig(context: Context, private val locationSettingsListener: LocationSettingsListener) : OnBroadcastDataReceived {
    private val context: Context = context.applicationContext

    private var additionalParams: MutableMap<String, String?> = HashMap()
    private var wifiManager: WifiManager? = null
    private var connectivityManager: ConnectivityManager? = null
    private var telephonyManager: TelephonyManager? = null
    private var locationManager: LocationManager? = null
    private var signalStrength = 0
    private var batteryLevel = 0.0
    private var currentLocation = DEFAULT_LOCATION
    private var cellularStateListener: MyCellularStateListener? = null
    private var batteryLevelBroadcastReceiver: BatteryLevelBroadcastReceiver? = null
    private var payload: Payload = Payload()
    private var heartbeat: Heartbeat = Heartbeat()
    private var networkHandler: NetworkHandler? = null

    companion object {
        private const val HEARTBEAT_INTERVAL_MILLIS = 10 * 60 * 1000
        private const val DEFAULT_LOCATION = "0.0,0.0"
        private const val LOCATION_INTERVAL_MILLIS = 30 * 1000
        private const val LOCATION_DISTANCE = 10f
        private var configParams = HeartbeatController.instance.configParams
    }

    init {
        HeartbeatController.instance.initSharedPrefs(context)
        initSystemServices()
        initNetworkConnection()
        listenForLocationUpdates()
    }

    private fun initNetworkConnection() {
        // decide whether to use http or ws
        initWebSocketConnection()
        // initHttpConnection()
    }

    private fun initHttpConnection() {
        networkHandler = HttpNetworkHandler()
    }


    private fun initWebSocketConnection() {
        networkHandler = WsNetworkHandler()
    }


    private fun initSystemServices() {
        wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        cellularStateListener = MyCellularStateListener(this)
        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }


    //Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());
    private val ipAddress: String
        get() = ip //Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());

    private val ip: String
        get() {
            var ipAddress = "0.0.0.0"
            try {
                val en = NetworkInterface.getNetworkInterfaces()
                while (en.hasMoreElements()) {
                    val intf = en.nextElement()
                    val enumIpAddr = intf.inetAddresses
                    while (enumIpAddr.hasMoreElements()) {
                        val inetAddress = enumIpAddr.nextElement()
                        if (!inetAddress.isLoopbackAddress && inetAddress is Inet4Address) {
                            Log.e("IP ADDRESS", " found address $ipAddress")
                            ipAddress = inetAddress.getHostAddress()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            Log.e("IP ADDRESS", " Here is the final Address $ipAddress")
            return ipAddress
        }


    private val networkType: String
        get() = try {
            val onWifi: Boolean
            val activeNetwork = connectivityManager!!.activeNetworkInfo

            activeNetwork?.let {
                onWifi = it.type == ConnectivityManager.TYPE_WIFI
                val connectivity = if (onWifi) Connectivity.WIFI else Connectivity.MOBILE_DATA
                connectivity.name
            } ?: Connectivity.UNKNOWN.name

        } catch (e: Exception) {
            e.printStackTrace()
            Connectivity.UNKNOWN.name
        }

    private fun getSignalStrength(): String {
        // signal strength being returned is cellular, so check if connected network is wifi
        return if (networkType.equals(Connectivity.WIFI.name, ignoreCase = true)) {
            wifiSignalStrength
        } else "$signalStrength dbm"
    }

    private fun getBatteryLevel(): String {
        return batteryLevel.toString()
    }

    // get connected network
    private val wifiSignalStrength: String
        get() {
            var connectedBSSID = ""
            var signalStrength = -1
            try {
                // get connected network
                val wifiInfo = wifiManager!!.connectionInfo
                if (wifiInfo != null) {
                    connectedBSSID = wifiInfo.bssid
                }
                val scanResults = wifiManager!!.scanResults
                for (result in scanResults) {
                    if (result.BSSID == connectedBSSID) {
                        signalStrength = WifiManager.calculateSignalLevel(result.level, 100)
                        break
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return signalStrength.toString()
        }


    private val isLocationDisabled: Boolean
        get() {
            val locationMode: Int = try {
                Settings.Secure.getInt(context.contentResolver, Settings.Secure.LOCATION_MODE)
            } catch (e: SettingNotFoundException) {
                e.printStackTrace()
                return true
            }
            return locationMode == Settings.Secure.LOCATION_MODE_OFF
        }

    // trigger callback back to device
    private val deviceLocation: String
        @SuppressLint("MissingPermission")
        get() {
            var result = DEFAULT_LOCATION
            if (isLocationDisabled) {
                // trigger callback back to device
                locationSettingsListener.onLocationDisabled()
                Log.e("TAG", "location turned off")
                return result
            }
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            var location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            if (location != null) {
                result = String.format("%s,%s", location.latitude, location.longitude)
                Log.e("Network Location", result)
            } else {
                Log.e("TAG", "network provider location null, attempt gps")
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (location != null) {
                    result = String.format("%s,%s", location.latitude, location.longitude)
                    Log.e("GPS Location", result)
                } else {
                    Log.e("TAG", "GPS provider location null, attempting passive")
                    location = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
                    if (location != null) {
                        result = String.format("%s,%s", location.latitude, location.longitude)
                        Log.e("Passive Location", result)
                    }
                }
            }
            Log.e("TAG", "result => $result")
            return result
        }


    @Throws(Exception::class)
    fun collectHeartbeatData() {
        if (isLocationDisabled) {
            // trigger callback back to device
            locationSettingsListener.onLocationDisabled()
            Log.e("TAG", "location turned off")
        }

        // get from controller
        configParams = HeartbeatController.instance.configParams
        additionalParams = HeartbeatController.instance.additionalParams

        // config params
        payload = Payload()
        payload.appId = configParams.appId
        payload.appName = configParams.appName
        payload.appVer = configParams.appVer
        payload.deviceId = configParams.deviceId
        payload.apiVersion = configParams.apiVersion // apiVersion

        // heartbeat params
        heartbeat = Heartbeat()
        heartbeat.ip_address = ipAddress
        heartbeat.network_type = networkType
        heartbeat.network_signal_strength = getSignalStrength()
        heartbeat.battery_life = getBatteryLevel()
        heartbeat.gps_address = if (isLocationDisabled) DEFAULT_LOCATION else currentLocation /*getDeviceLocation();*/
        heartbeat.heartbeat_version = BuildConfig.VERSION_NAME
    }

    fun sendMessageHandler() {
        val payload = getPayloadAsString()
        networkHandler?.sendMessage(payload)
        Log.e("TAG", "PAYLOAD => $payload")
    }

    /**
     * This method is for picking additional data params from persistence. It is
     * necessary because, when the POS restarts and we loose some data
     */
    private fun reconcileAdditionalData() {
        try {

            // get persisted object
            val persistedPayload: String = HeartbeatController.instance.lastPayload

            // if empty do nothing
            if (persistedPayload.isEmpty()) {
                Log.e("TAG", "No persisted payload found")
                return
            }
            Log.e("TAG", "persisted payload => $persistedPayload")
            val persistedHeartbeat = JSONObject(persistedPayload).optJSONObject("hb")

            // check if we have a valid persisted payload
            if (persistedHeartbeat == null) {
                Log.e("TAG", "persisted payload has no \"hb\" object hence exiting reconciliation")
                return
            }

            // check if we are dealing with the same merchant
            val persistedMerchantId = persistedHeartbeat.optString("ms_id", "")
            val currentMerchantId = if (additionalParams["ms_id"] == null) "" else additionalParams["ms_id"]!!

            // if current merchant id is empty, it means the POS is not logged in yet.
            if (currentMerchantId.isNotEmpty() && currentMerchantId.equals(persistedMerchantId, ignoreCase = true)) {

                // proceed with reconciliation
                for (key in additionalParams.keys) {
                    val value = additionalParams[key]

                    // do not reconcile error
                    if (key.equals("err", ignoreCase = true)) {
                        continue
                    }

                    // if current payload additional data has values that are empty
                    if (value!!.isEmpty()) {

                        // check if the persisted heartbeat has the value we want
                        val persistedValue = persistedHeartbeat.optString(key, "")

                        // replace the current values with values from persistence if they exist
                        if (persistedValue.isNotEmpty()) {
                            additionalParams[key] = persistedValue
                            Log.e("Heartbeat", "reconciling $key")
                        }
                    }
                }
            } else {
                // different merchant. Do no not reconcile
                Log.e("Heartbeat", "No reconcile necessary, User not logged in or different merchant detected")
            }
        } catch (e: Exception) {
            Log.e("TAG", "reconcile failed")
            e.printStackTrace()
        }
    }

    private fun mergeHeartbeatAndAdditionalParams(): JSONObject {
        var result = JSONObject()

        // convert heartbeat object into json
        val gsonBuilder = GsonBuilder().create()
        val jsonStr = gsonBuilder.toJson(heartbeat)


        // reconcile additional data first
        reconcileAdditionalData()
        try {
            val obj = JSONObject(jsonStr)
            for (key in additionalParams.keys) {
                obj.put(key, additionalParams[key])
            }

            // add a timestamp
            val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            obj.put("ts", date)
            result = obj
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    // add heartbeat to json
    fun getPayloadAsString(): String {

        if (payload == null) {
            Log.e("TAG", "Payload is null")
            return ""
        }

        var message = ""
        try {
            val gsonBuilder = GsonBuilder().create()
            message = gsonBuilder.toJson(payload)

            // add heartbeat to json
            val o = JSONObject(message)
            o.put("hb", mergeHeartbeatAndAdditionalParams())
            message = o.toString()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return message
    }

    fun startService() {

        // listen on telephony service
        telephonyManager?.listen(cellularStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS or PhoneStateListener.LISTEN_DATA_CONNECTION_STATE)

        // start broadcast receiver
        val batteryIntentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        batteryLevelBroadcastReceiver = BatteryLevelBroadcastReceiver(this)
        context.registerReceiver(batteryLevelBroadcastReceiver, batteryIntentFilter)

        determineJobService()
    }

    private fun determineJobService() {
        // useWorkManager()
        initAlarmManager()
    }

    private fun useWorkManager() {
        // set up constraints
        val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(false)
                .build()

        val heartbeatWork  = PeriodicWorkRequest
                .Builder(HeartbeatWorker::class.java, configParams.triggerIntervalMillis.toLong(), TimeUnit.MILLISECONDS)
                .setConstraints(constraints)
                .build()

        WorkManager.getInstance()
                .enqueueUniquePeriodicWork("HEARTBEAT", ExistingPeriodicWorkPolicy.KEEP, heartbeatWork);
    }

    private fun initAlarmManager() {

        // default interval is 10 minutes
        Log.e("TAG", "trigger time = " + configParams.triggerIntervalMillis)
        val intervalMillis = if (configParams.triggerIntervalMillis == 0) HEARTBEAT_INTERVAL_MILLIS else configParams.triggerIntervalMillis
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), intervalMillis.toLong(), pendingIntent)
        try {
            Log.e("TAG", "attempt immediate send")
            HeartbeatController.instance.sendImmediately()
        } catch (e: Exception) {
            Log.e("TAG", "could not send initial heartbeat")
            e.printStackTrace()
        }
    }

    fun destroyService() {
        if (telephonyManager != null) {
            telephonyManager!!.listen(cellularStateListener, PhoneStateListener.LISTEN_NONE)
        }
        context.unregisterReceiver(batteryLevelBroadcastReceiver)
        additionalParams.clear()
        // locationManager?.removeUpdates(locationListener)


        // WorkManager.getInstance().cancelAllWork();
    }

    // signal strength being returned is cellular
    override fun onSignalStrengthChanged(value: Int) {
        signalStrength = value
    }

    override fun onBatteryLevelChanged(level: Int) {
        batteryLevel = level.toDouble() / 100
    }


    @SuppressLint("MissingPermission")
    private fun listenForLocationUpdates() {
        if (locationManager == null) {
            Log.e("TAG", "location manager null, recreating")
            locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        }

        locationManager?.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                LOCATION_INTERVAL_MILLIS.toLong(),
                LOCATION_DISTANCE,
                object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        currentLocation = String.format("%s,%s", location.latitude, location.longitude)
                        Log.e("Location Update", "location = $currentLocation")
                    }

                    override fun onStatusChanged(provider: String, i: Int, bundle: Bundle) {
                        val state = when (i) {
                            0 -> "OUT OF SERVICE"
                            1 -> "TEMPORARILY UNAVAILABLE"
                            2 -> "AVAILABLE"
                            else -> ""
                        }
                        Log.e("TAG", "onStatusChanged: " + provider + "current state: " + state)
                    }

                    override fun onProviderEnabled(provider: String) {
                        Log.e("TAG", "onProviderEnabled: $provider")
                    }

                    override fun onProviderDisabled(provider: String) {
                        Log.e("TAG", "onProviderDisabled: $provider")
                    }
                }
        )
    }
}