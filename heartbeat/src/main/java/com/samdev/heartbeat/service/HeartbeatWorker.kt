package com.samdev.heartbeat.service

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.samdev.heartbeat.HeartbeatController

class HeartbeatWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val context = applicationContext
        val heartbeatConfig = HeartbeatController.instance.heartbeatConfig ?: return Result.retry()

        heartbeatConfig.sendMessageHandler()
        return Result.success()
    }
}