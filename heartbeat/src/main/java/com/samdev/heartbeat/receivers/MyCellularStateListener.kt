package com.samdev.heartbeat.receivers

import android.net.ConnectivityManager
import android.telephony.PhoneStateListener
import android.telephony.SignalStrength
import android.telephony.TelephonyManager
import android.util.Log
import com.samdev.heartbeat.callbacks.OnBroadcastDataReceived

class MyCellularStateListener(private val onBroadcastDataReceived: OnBroadcastDataReceived) : PhoneStateListener() {

    private val TAG = "TAG"

    override fun onDataConnectionStateChanged(state: Int, networkType: Int) {
        super.onDataConnectionStateChanged(state, networkType)
        val s = if (networkType == ConnectivityManager.TYPE_WIFI) "WIFI" else "MOBILE DATA"
        when (state) {
            TelephonyManager.DATA_DISCONNECTED -> Log.i(TAG, "onDataConnectionStateChanged: DATA_DISCONNECTED")
            TelephonyManager.DATA_CONNECTING -> Log.i(TAG, "onDataConnectionStateChanged: DATA_CONNECTING")
            TelephonyManager.DATA_CONNECTED -> Log.i(TAG, "onDataConnectionStateChanged: DATA_CONNECTED")
            TelephonyManager.DATA_SUSPENDED -> Log.i(TAG, "onDataConnectionStateChanged: DATA_SUSPENDED")
            else -> Log.w(TAG, "onDataConnectionStateChanged: UNKNOWN $state")
        }
    }

    override fun onSignalStrengthsChanged(signalStrength: SignalStrength) {
        super.onSignalStrengthsChanged(signalStrength)
        // val j = signalStrength.cellSignalStrengths[0].dbm
        var i = signalStrength.gsmSignalStrength
        i = 2 * i - 113
        onBroadcastDataReceived.onSignalStrengthChanged(i)
    }
}