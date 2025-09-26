package com.example.kh_studyprojects_weatherapp.di

import com.example.kh_studyprojects_weatherapp.data.api.BackendRetrofitConfig
import com.example.kh_studyprojects_weatherapp.data.api.ExternalRetrofitConfig
import com.example.kh_studyprojects_weatherapp.data.api.kakao.KakaoApiService
import com.example.kh_studyprojects_weatherapp.data.api.sidemenu.SmFavoriteLocationApiService
import com.example.kh_studyprojects_weatherapp.data.api.weather.WeatherApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 네트워크 DI 모듈
 * - Retrofit 기반 API 서비스를 의존성 주입으로 제공합니다.
 * - 프레젠테이션/도메인 레이어에서 Retrofit 팩토리를 직접 접근하지 않도록 캡슐화합니다.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // Kakao API 서비스 제공
    @Provides
    @Singleton
    fun provideKakaoApiService(): KakaoApiService =
        ExternalRetrofitConfig.kakaoRetrofit.create(KakaoApiService::class.java)
    
    // Weather API 서비스 제공
    @Provides
    @Singleton
    fun provideWeatherApiService(): WeatherApiService =
        BackendRetrofitConfig.retrofit.create(WeatherApiService::class.java)

    // SmFavoriteLocation API 서비스 제공
    @Provides
    @Singleton
    fun provideSmFavoriteLocationApiService(): SmFavoriteLocationApiService =
        BackendRetrofitConfig.retrofit.create(SmFavoriteLocationApiService::class.java)
}
