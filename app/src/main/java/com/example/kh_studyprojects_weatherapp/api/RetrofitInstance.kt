package com.example.kh_studyprojects_weatherapp.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import com.example.kh_studyprojects_weatherapp.BuildConfig

object RetrofitInstance {

    // HttpLoggingInterceptor 설정
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    val apiUrl = if (BuildConfig.DEBUG) {
        "http://10.0.2.2:8080"  // 에뮬레이터
    } else {
        "http://172.30.1.29:8080"  // 실제 디바이스
    }


    // OkHttpClient 설정
    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor) // 로깅 인터셉터 추가
        .connectTimeout(30, TimeUnit.SECONDS) // 연결 타임아웃 설정
        .readTimeout(30, TimeUnit.SECONDS) // 읽기 타임아웃 설정
        .writeTimeout(30, TimeUnit.SECONDS) // 쓰기 타임아웃 설정
        .build()

    // Retrofit 설정
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080") // 실제 API Base URL로 수정
            .addConverterFactory(GsonConverterFactory.create()) // JSON 직렬화/역직렬화 처리
            .client(client) // 설정한 OkHttpClient를 Retrofit에 추가
            .build()
    }

    val api: WeatherApiService by lazy {
        retrofit.create(WeatherApiService::class.java)
    }
}




