package com.samdev.heartbeat.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.samdev.heartbeat.HeartbeatController

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val heartbeatConfig = HeartbeatController.instance.heartbeatConfig ?: return
        try {

            //val s = heartbeatConfig.getPayloadAsString();
            //Log.e("PAYLOAD => ", s)

            heartbeatConfig.collectHeartbeatData()
            heartbeatConfig.sendMessageHandler()
        } catch (ignored: Exception) {
        }
    }
}