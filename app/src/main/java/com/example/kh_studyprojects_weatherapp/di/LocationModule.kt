package com.example.kh_studyprojects_weatherapp.di

import com.example.kh_studyprojects_weatherapp.presentation.common.location.InMemoryLocationSelectionStore
import com.example.kh_studyprojects_weatherapp.presentation.common.location.LocationSelectionStore
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt 모듈: 위치 선택 상태 저장소 바인딩을 제공합니다.
 *
 * - 앱 전역에서 `LocationSelectionStore` 인터페이스의 구현체를 주입하기 위해 사용됩니다.
 * - `SingletonComponent` 에 설치되어 애플리케이션 수명 동안 단일 인스턴스를 유지합니다.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class LocationModule {
    /**
     * `LocationSelectionStore` 인터페이스에 대한 구현체 바인딩입니다.
     *
     * @param impl 메모리 기반 구현체 (`InMemoryLocationSelectionStore`)
     * @return 앱 전역에서 주입되는 `LocationSelectionStore` 싱글톤
     */
    @Binds
    @Singleton
    abstract fun bindLocationSelectionStore(impl: InMemoryLocationSelectionStore): LocationSelectionStore
}


