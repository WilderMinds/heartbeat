package com.samdev.heartbeat.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import com.samdev.heartbeat.callbacks.OnBroadcastDataReceived

class BatteryLevelBroadcastReceiver(private val onBroadcastDataReceived: OnBroadcastDataReceived) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // receive just once until needed again
        // context.unregisterReceiver(this);
        val rawLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        var level = -1
        if (rawLevel >= 0 && scale > 0) {
            level = rawLevel * 100 / scale
        }
        onBroadcastDataReceived.onBatteryLevelChanged(level)
    }
}