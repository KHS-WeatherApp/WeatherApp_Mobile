package com.example.kh_studyprojects_weatherapp.di

import android.os.Build
import android.util.Log
import com.example.kh_studyprojects_weatherapp.BuildConfig
import com.example.kh_studyprojects_weatherapp.data.api.kakao.KakaoApiService
import com.example.kh_studyprojects_weatherapp.data.api.sidemenu.SmFavoriteLocationApiService
import com.example.kh_studyprojects_weatherapp.data.api.weather.WeatherApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * 네트워크 설정 상수
 */
private object NetworkConstants {
    const val CONNECT_TIMEOUT = 30L
    const val READ_TIMEOUT = 30L
    const val WRITE_TIMEOUT = 30L
    const val KAKAO_BASE_URL = "https://dapi.kakao.com/"
}

/**
 * Qualifier 어노테이션으로 Retrofit 인스턴스 구분
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BackendRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class KakaoRetrofit

/**
 * 네트워크 DI 모듈
 * - 모든 Retrofit 및 OkHttpClient 설정을 중앙에서 관리
 * - Qualifier를 통해 여러 Retrofit 인스턴스 구분
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    private const val TAG = "NetworkModule"

    /**
     * 백엔드 서버 Base URL 제공
     */
    @Provides
    @Singleton
    fun provideBackendBaseUrl(): String {
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
        return resolved
    }

    /**
     * 공통 OkHttpClient (로깅 설정)
     */
    private fun createBaseOkHttpClient(enableLogging: Boolean, tag: String): OkHttpClient.Builder {
        return OkHttpClient.Builder()
            .connectTimeout(NetworkConstants.CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(NetworkConstants.READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(NetworkConstants.WRITE_TIMEOUT, TimeUnit.SECONDS)
            .apply {
                if (enableLogging) {
                    addInterceptor(HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BODY
                    })
                    addInterceptor { chain ->
                        val request = chain.request()
                        Log.d(tag, "Request URL: ${request.url}")
                        Log.d(tag, "Request headers: ${request.headers}")
                        chain.proceed(request)
                    }
                }
            }
            .retryOnConnectionFailure(true)
    }

    /**
     * 백엔드 서버용 OkHttpClient
     */
    @Provides
    @Singleton
    @BackendRetrofit
    fun provideBackendOkHttpClient(): OkHttpClient {
        return createBaseOkHttpClient(
            enableLogging = BuildConfig.ENABLE_BACKEND_HTTP_LOGGING,
            tag = "BackendAPI"
        ).build()
    }

    /**
     * Kakao API용 OkHttpClient (Authorization 헤더 추가)
     */
    @Provides
    @Singleton
    @KakaoRetrofit
    fun provideKakaoOkHttpClient(): OkHttpClient {
        return createBaseOkHttpClient(
            enableLogging = BuildConfig.ENABLE_EXTERNAL_HTTP_LOGGING,
            tag = "KakaoAPI"
        ).addInterceptor { chain ->
            val original = chain.request()
            val apiKey = BuildConfig.KAKAO_API_KEY.trim()
            val request = original.newBuilder()
                .header("Authorization", "KakaoAK $apiKey")
                .method(original.method, original.body)
                .build()
            chain.proceed(request)
        }.build()
    }

    /**
     * 백엔드 서버용 Retrofit
     */
    @Provides
    @Singleton
    @BackendRetrofit
    fun provideBackendRetrofit(
        baseUrl: String,
        @BackendRetrofit okHttpClient: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * Kakao API용 Retrofit
     */
    @Provides
    @Singleton
    @KakaoRetrofit
    fun provideKakaoRetrofit(
        @KakaoRetrofit okHttpClient: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(NetworkConstants.KAKAO_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * Weather API 서비스 제공
     */
    @Provides
    @Singleton
    fun provideWeatherApiService(
        @BackendRetrofit retrofit: Retrofit
    ): WeatherApiService = retrofit.create(WeatherApiService::class.java)

    /**
     * SmFavoriteLocation API 서비스 제공
     */
    @Provides
    @Singleton
    fun provideSmFavoriteLocationApiService(
        @BackendRetrofit retrofit: Retrofit
    ): SmFavoriteLocationApiService = retrofit.create(SmFavoriteLocationApiService::class.java)

    /**
     * Kakao API 서비스 제공
     */
    @Provides
    @Singleton
    fun provideKakaoApiService(
        @KakaoRetrofit retrofit: Retrofit
    ): KakaoApiService = retrofit.create(KakaoApiService::class.java)
}
