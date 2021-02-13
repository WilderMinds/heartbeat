package com.expresspay.heartbeat.callbacks;

public interface OnBroadcastDataReceived {
    void onSignalStrengthChanged(int value);
    void onBatteryLevelChanged(int level);
}
