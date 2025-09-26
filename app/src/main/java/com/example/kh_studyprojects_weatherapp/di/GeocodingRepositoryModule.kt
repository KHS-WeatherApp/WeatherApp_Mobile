package com.example.kh_studyprojects_weatherapp.di

import com.example.kh_studyprojects_weatherapp.data.repository.common.geocoding.GeocodingRepositoryImpl
import com.example.kh_studyprojects_weatherapp.domain.repository.common.geocoding.GeocodingRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * GeocodingRepository 바인딩 모듈
 * - 인터페이스와 구현체를 싱글톤 범위로 연결합니다.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class GeocodingRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindGeocodingRepository(impl: GeocodingRepositoryImpl): GeocodingRepository
}
