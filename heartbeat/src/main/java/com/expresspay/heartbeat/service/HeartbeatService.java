package com.expresspay.heartbeat.service;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.expresspay.heartbeat.HeartbeatConfig;
import com.expresspay.heartbeat.controller.HeartbeatController;

public class HeartbeatService extends Worker {

    public HeartbeatService(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {

        Context context = getApplicationContext();

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

        return Result.success();
    }
}
