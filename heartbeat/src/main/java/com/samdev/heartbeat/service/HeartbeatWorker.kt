package com.samdev.heartbeat.service

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class HeartbeatWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val context = applicationContext

//        HeartbeatConfig heartbeatConfig = HeartbeatController.getInstance().getHeartbeatConfig();
//        if (heartbeatConfig == null) {
//            return Result.retry();
//        }
//
////        Payload payload = heartbeatConfig.collectHeartbeatData();
////        Log.e("PAYLOAD", payload.toString());
//
//
//        heartbeatConfig.sendMessageHandler();
        return Result.success()
    }
}