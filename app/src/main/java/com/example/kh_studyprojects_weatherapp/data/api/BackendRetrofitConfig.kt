package com.example.kh_studyprojects_weatherapp.data.api

import android.os.Build
import android.util.Log
import com.example.kh_studyprojects_weatherapp.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object BackendRetrofitConfig {
    private const val TAG = "BackendRetrofitConfig"

    private val baseUrl: String by lazy {
        val resolved = if (Build.FINGERPRINT.contains("generic")) {
            BuildConfig.BACKEND_EMULATOR_BASE_URL
        } else {
            BuildConfig.BACKEND_BASE_URL
        }.ifBlank {
            throw IllegalStateException("Backend base URL is not configured. Check BuildConfig settings.")
        }

        if (BuildConfig.ENABLE_BACKEND_HTTP_LOGGING) {
            Log.d(TAG, "Using backend base URL: $resolved")
        }
        resolved
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .apply {
                if (BuildConfig.ENABLE_BACKEND_HTTP_LOGGING) {
                    addInterceptor(HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BODY
                    })
                    addInterceptor { chain ->
                        val request = chain.request()
                        Log.d(TAG, "Request URL: ${'$'}{request.url}")
                        Log.d(TAG, "Request headers: ${'$'}{request.headers}")
                        chain.proceed(request)
                    }
                }
            }
            .retryOnConnectionFailure(true)
            .build()
    }

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
