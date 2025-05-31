package com.example.kh_studyprojects_weatherapp.di

import android.content.Context
import android.location.Geocoder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.Locale
import javax.inject.Singleton

/**
 * Hilt 의존성 주입을 위한 모듈
 * 앱 전체에서 사용되는 의존성을 제공하는 클래스
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    /**
     * Application Context를 제공하는 함수
     * 앱 전체에서 사용되는 Context를 싱글톤으로 제공
     * 
     * @param context Application Context
     * @return Context 앱의 Application Context
     */
    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context {
        return context
    }

    /**
     * Geocoder 인스턴스를 제공하는 함수
     * 위도/경도를 주소로 변환하는데 사용되는 Geocoder를 싱글톤으로 제공
     * 한국어 로케일을 사용하여 주소를 한국어로 반환
     * 
     * @param context Application Context
     * @return Geocoder 한국어 로케일이 설정된 Geocoder 인스턴스
     */
    @Provides
    @Singleton
    fun provideGeocoder(@ApplicationContext context: Context): Geocoder {
        return Geocoder(context, Locale.KOREA)
    }
} 