package com.samdev.heatbeatapplication

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.samdev.heartbeat.HeartbeatController
import com.samdev.heartbeat.callbacks.ApplicationCallbacks
import com.samdev.heartbeat.models.AppIdentifier
import com.samdev.heartbeat.models.ConfigParams
import org.json.JSONObject
import java.util.*

class MainActivity : AppCompatActivity(), ApplicationCallbacks {

    companion object {
        private const val LOCATION_PERMISSION_REQUEST = 195
    }

    private var textView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        textView = findViewById(R.id.tv_heartbeat)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions((this as Activity), arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION), LOCATION_PERMISSION_REQUEST)
            Log.e("TAG", "no permission granted")
        } else {
            Log.e("TAG", "permission granted, we move")
            initHeartbeat()
        }

        /*findViewById<View>(R.id.clear).setOnClickListener {
            val map: MutableMap<String, String> = HashMap()
            map["ms_id"] = ""
            map["ms_name"] = ""
            map["card_ts"] = ""
            map["mm_ts"] = ""
            HeartbeatController.instance.setAdditionalParams(map)
        }*/
    }

    /**
     * If you want to include params that are specific to your app to,
     * the heartbeat payload, set them up using a Map.
     *
     * Include each new/additional param as a key in the map
     *
     * @return map that contains all the addition
     */
    private fun initAdditionalData(): Map<String, String> {
        val map: MutableMap<String, String> = HashMap()
        map["last_db_sync"] = "0123456789"
        map["some_other_data"] = "set here"
        /*map["card_ts"] = ""
        map["mm_ts"] = ""
        map["err"] = ""
        map["err_ts"] = ""*/
        return map
    }


    /**
     * Initialize the AppIndentifier.
     *
     * @see AppIdentifier
     * @return your appIdentifier object
     */
    private fun initAppIdentifier(): AppIdentifier {
        val appIdentifier = AppIdentifier()
        // appIdentifier.setDeviceId("0000001234567890");
        appIdentifier.appId = BuildConfig.APPLICATION_ID
        appIdentifier.appName = getString(R.string.app_name)
        appIdentifier.appVer = BuildConfig.VERSION_NAME
        return appIdentifier
    }


    /**
     * Initialize the heartbeat service configurations
     *
     * @see ConfigParams
     * @return your heartbeat configurations
     */
    private fun initConfigParams(): ConfigParams {
        val ws_url = "wss://your.domain.com/wsapp/hb/sub/"
        val http_url = "https://api.mocki.io/v1/d85bee89/"
        val configParams = ConfigParams()

        configParams.networkUrl = http_url
        configParams.triggerIntervalMillis = 1 * 60 * 1000
        configParams.isPersistSocketConnection = false
        configParams.trackDeviceLocation = true
        configParams.addIdentifier = initAppIdentifier()
        return configParams
    }


    /**
     * Start up the heartbeat service by first making sure the
     * `ConfigParams` is set.
     *
     * Also set the additional params if any
     */
    private fun initHeartbeat() {

        // set up config params
        val configParams = initConfigParams()

        // set up additional data if any
        val additionalParams = initAdditionalData()

        // assign
        HeartbeatController.instance.configParams = configParams
        HeartbeatController.instance.setAdditionalParams(additionalParams)

        // start the heartbeat service
        try {
            HeartbeatController.instance.startService(this, this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        HeartbeatController.instance.destroyService()
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            var allGranted = true

            // if either is not granted
            for (result in grantResults) {
                packageManager
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false
                    break
                }
            }
            if (allGranted) {
                // we good
                initHeartbeat()
            } else {
            }
        }
    }

    override fun onLocationDisabled() {
        Toast.makeText(this, "User location is disabled, Logging you out", Toast.LENGTH_SHORT).show()
    }

    override fun onLocationEnabled() {}


    override fun onHeartbeatPayloadReceived(payload: String?) {
        payload?.let {
            Toast.makeText(this, "New payload received", Toast.LENGTH_SHORT).show()
            textView?.text = formatJsonString(it)
        }
    }

    private fun formatJsonString(str: String) : String {
        val jsonObject = JSONObject(str)
        return jsonObject.toString(4)
    }

    override fun onNetworkError(throwable: Throwable) {}

    override fun onNetworkSuccess(payload: Any?) {}
}