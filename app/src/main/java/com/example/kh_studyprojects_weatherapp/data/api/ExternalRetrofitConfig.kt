package com.example.kh_studyprojects_weatherapp.data.api

import android.util.Log
import com.example.kh_studyprojects_weatherapp.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ExternalRetrofitConfig {
    private const val TAG = "ExternalRetrofitConfig"
    private const val KAKAO_BASE_URL = "https://dapi.kakao.com/"

    private val kakaoOkHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .apply {
                if (BuildConfig.ENABLE_EXTERNAL_HTTP_LOGGING) {
                    addInterceptor(HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BODY
                    })
                }
            }
            .addInterceptor { chain ->
                val original = chain.request()
                val request = original.newBuilder()
                    .header("Authorization", "KakaoAK ${'$'}{BuildConfig.KAKAO_API_KEY}")
                    .method(original.method, original.body)
                    .build()

                if (BuildConfig.ENABLE_EXTERNAL_HTTP_LOGGING) {
                    Log.d(TAG, "Kakao API request URL: ${'$'}{request.url}")
                }
                chain.proceed(request)
            }
            .retryOnConnectionFailure(true)
            .build()
    }

    val kakaoRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(KAKAO_BASE_URL)
            .client(kakaoOkHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
