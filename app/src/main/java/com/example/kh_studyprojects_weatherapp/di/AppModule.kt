package com.example.kh_studyprojects_weatherapp.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt 의존성 주입을 위한 모듈
 * 앱 전체에서 사용되는 의존성을 제공하는 클래스
 * 
 * @author 김효동
 * @since 2025.08.06
 * @version 2.0 (Geocoder 제거, 카카오 API 적용)
 * 
 * <pre>
 * << 개정이력(Modification Information) >>
 *
 * 수정일		수정자	수정내용
 * ----------	------	---------------------------
 * 2025.??.??	김효동	최초 생성
 * 2025.08.06	김효동	Geocoder 의존성 제거 (카카오 API로 대체)
 * </pre>
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
    
    // Note: Geocoder 의존성은 카카오 로컬 API로 대체되어 제거됨
    // 카카오 API는 ExternalApiRetrofitInstance에서 관리됨
} 