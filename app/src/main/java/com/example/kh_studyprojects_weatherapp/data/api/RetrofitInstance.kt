package com.example.kh_studyprojects_weatherapp.data.api

import android.os.Build
import android.util.Log
import com.example.kh_studyprojects_weatherapp.data.api.weather.WeatherApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {
    private const val DEVICE_URL = "http://192.168.126.40:8080"  // 내부 WiFi IP 김효동
//    private const val DEVICE_URL = "http://192.168.0.65:8080"  // 내부 WiFi IP 김효동 회사
//    private const val DEVICE_URL = "http://172.30.1.53:8080"  // 내부 WiFi IP 김지윤
    private const val EMULATOR_URL = "http://10.0.2.2:8080"

    // 에뮬레이터 여부에 따라 URL 선택
    private val BASE_URL = if (Build.FINGERPRINT.contains("generic")) {
        EMULATOR_URL.also { Log.d("RetrofitInstance", "에뮬레이터 URL 사용: $it") }
    } else {
        DEVICE_URL.also { Log.d("RetrofitInstance", "실제 기기 URL 사용: $it") }
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .addInterceptor { chain ->
            val request = chain.request()
            Log.d("RetrofitInstance", "요청 URL: ${request.url}")
            Log.d("RetrofitInstance", "요청 헤더: ${request.headers}")
            chain.proceed(request)
        }
        .retryOnConnectionFailure(true)  // 연결 실패시 재시도
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val weatherApiService: WeatherApiService = retrofit.create(WeatherApiService::class.java)
}