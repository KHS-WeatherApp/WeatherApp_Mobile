package com.example.kh_studyprojects_weatherapp.data.api

import android.util.Log
import com.example.kh_studyprojects_weatherapp.BuildConfig
import com.example.kh_studyprojects_weatherapp.data.api.kakao.KakaoApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * 외부 API Retrofit 인스턴스 관리 클래스
 * 
 * 카카오, 네이버 등 외부 공개 API와의 통신을 담당합니다.
 * 각 API별로 인증 헤더와 설정을 관리합니다.
 *
 * @author 김효동
 * @since 2025.08.06
 * @version 1.0
 */
object ExternalApiRetrofitInstance {
    
    /** 카카오 로컬 API 기본 URL */
    private const val KAKAO_BASE_URL = "https://dapi.kakao.com/"

    /** 카카오 API용 OkHttpClient (인증 헤더 포함) */
    private val kakaoOkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .addInterceptor { chain ->
            val original = chain.request()
            val request = original.newBuilder()
                .header("Authorization", "KakaoAK ${BuildConfig.KAKAO_API_KEY}") // BuildConfig에서 API 키 가져오기
                .method(original.method, original.body)
                .build()
            Log.d("ExternalApiRetrofitInstance", "카카오 API 요청 URL: ${request.url}")
            chain.proceed(request)
        }
        .retryOnConnectionFailure(true)
        .build()

    /** 카카오 API용 Retrofit 인스턴스 */
    private val kakaoRetrofit = Retrofit.Builder()
        .baseUrl(KAKAO_BASE_URL)
        .client(kakaoOkHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    /** 카카오 로컬 API 서비스 인스턴스 */
    val kakaoApiService: KakaoApiService by lazy {
        kakaoRetrofit.create(KakaoApiService::class.java)
    }
    
    // 향후 다른 외부 API 추가 시 여기에 추가
    // 예: 네이버 지도 API, 구글 플레이스 API 등
} 