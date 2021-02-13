package com.samdev.heartbeat.network.retrofit

import com.samdev.heartbeat.HeartbeatController
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitClient {

    companion object {

        private val baseUrl: String = HeartbeatController.instance.configParams.networkUrl

        private val okHttpClient: OkHttpClient
            get() {
                val loggingInterceptor = HttpLoggingInterceptor()
                loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

                return OkHttpClient.Builder()
                        .addInterceptor(loggingInterceptor)
                        .build()
            }

        val apiService: ApiService by lazy {
            Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient)
                    .build().create(ApiService::class.java)
        }
    }


}