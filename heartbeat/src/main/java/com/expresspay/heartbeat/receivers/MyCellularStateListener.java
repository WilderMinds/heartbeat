package com.expresspay.heartbeat.receivers;

import android.net.ConnectivityManager;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.expresspay.heartbeat.callbacks.OnBroadcastDataReceived;

public class MyCellularStateListener extends PhoneStateListener {

    private OnBroadcastDataReceived onBroadcastDataReceived;

    public MyCellularStateListener(OnBroadcastDataReceived onBroadcastDataReceived) {
        this.onBroadcastDataReceived = onBroadcastDataReceived;
    }

    @Override
    public void onDataConnectionStateChanged(int state, int networkType) {
        super.onDataConnectionStateChanged(state, networkType);
        String LOG_TAG = "TAG";


        String s = networkType == ConnectivityManager.TYPE_WIFI ? "WIFI" : "MOBILE DATA";


        switch (state) {
            case TelephonyManager.DATA_DISCONNECTED:
                Log.i(LOG_TAG, "onDataConnectionStateChanged: DATA_DISCONNECTED");
                break;
            case TelephonyManager.DATA_CONNECTING:
                Log.i(LOG_TAG, "onDataConnectionStateChanged: DATA_CONNECTING");
                break;
            case TelephonyManager.DATA_CONNECTED:
                Log.i(LOG_TAG, "onDataConnectionStateChanged: DATA_CONNECTED");
                break;
            case TelephonyManager.DATA_SUSPENDED:
                Log.i(LOG_TAG, "onDataConnectionStateChanged: DATA_SUSPENDED");
                break;
            default:
                Log.w(LOG_TAG, "onDataConnectionStateChanged: UNKNOWN " + state);
                break;
        }
    }

    @Override
    public void onSignalStrengthsChanged(SignalStrength signalStrength) {
        super.onSignalStrengthsChanged(signalStrength);
        // int i = signalStrength.getCellSignalStrengths().get(0).getDbm();
        int i  = signalStrength.getGsmSignalStrength();
        i = (2 * i) - 113;
        onBroadcastDataReceived.onSignalStrengthChanged(i);
    }

}
