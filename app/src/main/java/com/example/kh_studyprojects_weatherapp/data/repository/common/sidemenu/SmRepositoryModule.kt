package com.example.kh_studyprojects_weatherapp.data.repository.common.sidemenu

import com.example.kh_studyprojects_weatherapp.domain.repository.common.sidemenu.SmFavoriteLocationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 사이드메뉴 관련 Repository 모듈
 * 
 * Hilt 의존성 주입을 위한 Repository 바인딩을 제공합니다.
 *
 * @author 김효동
 * @since 2025.08.26
 * @version 3.0 (통합 최적화 버전)
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class SmRepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindFavoriteLocationRepository(
        smRepository: SmRepository
    ): SmFavoriteLocationRepository
}
