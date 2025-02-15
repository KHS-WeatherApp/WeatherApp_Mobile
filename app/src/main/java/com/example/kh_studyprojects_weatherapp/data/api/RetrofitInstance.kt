package com.example.kh_studyprojects_weatherapp.data.api.weather

import android.os.Build
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {
    private const val DEVICE_URL = "http://172.30.1.53:8080"  // 내부 WiFi IP
    private const val EMULATOR_URL = "http://10.0.2.2:8080"

    // 에뮬레이터 여부에 따라 URL 선택
    private val BASE_URL = if (Build.FINGERPRINT.contains("generic")) {
        EMULATOR_URL
    } else {
        DEVICE_URL
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .retryOnConnectionFailure(true)  // 연결 실패시 재시도
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val weatherApiService: WeatherApiService = retrofit.create(WeatherApiService::class.java)
}