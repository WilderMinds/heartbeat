package com.samdev.heartbeat.network.retrofit

import com.google.gson.JsonObject
import com.samdev.heartbeat.HeartbeatController
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    @POST("/")
    suspend fun uploadHeartbeat(@Body payload: JsonObject): JsonObject
}