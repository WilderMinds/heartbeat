package com.expresspay.heartbeat.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.expresspay.heartbeat.HeartbeatConfig;
import com.expresspay.heartbeat.controller.HeartbeatController;
import com.expresspay.heartbeat.models.Payload;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        HeartbeatConfig heartbeatConfig = HeartbeatController.getInstance().getHeartbeatConfig();

        if (heartbeatConfig == null) {
            return;
        }

        try {

            /*String s = heartbeatConfig.getPayloadAsString();
            Log.e("PAYLOAD => ", s);*/

            heartbeatConfig.collectHeartbeatData();
            heartbeatConfig.sendMessageHandler();

        } catch (Exception ignored) {}
    }
}
