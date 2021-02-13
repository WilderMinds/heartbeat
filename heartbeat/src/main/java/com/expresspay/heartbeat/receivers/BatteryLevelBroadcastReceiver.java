package com.expresspay.heartbeat.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;

import com.expresspay.heartbeat.callbacks.OnBroadcastDataReceived;

public class BatteryLevelBroadcastReceiver extends BroadcastReceiver {

    private OnBroadcastDataReceived onBroadcastDataReceived;

    public BatteryLevelBroadcastReceiver(OnBroadcastDataReceived onBroadcastDataReceived) {
        this.onBroadcastDataReceived = onBroadcastDataReceived;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // receive just once until needed again
        // context.unregisterReceiver(this);

        int rawLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        int level = -1;
        if (rawLevel >= 0 && scale > 0) {
            level = (rawLevel * 100) / scale;
        }

        onBroadcastDataReceived.onBatteryLevelChanged(level);
    }
}
