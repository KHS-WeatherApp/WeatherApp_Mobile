package com.example.kh_studyprojects_weatherapp.presentation.weather.additional

import com.example.kh_studyprojects_weatherapp.domain.model.weather.WeatherAdditional
import com.example.kh_studyprojects_weatherapp.domain.repository.weather.WeatherRepository
import com.example.kh_studyprojects_weatherapp.presentation.common.base.BaseLoadViewModel
import com.example.kh_studyprojects_weatherapp.presentation.common.location.EffectiveLocationResolver
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * 추가 정보(대기질/UV 등) 화면의 ViewModel
 * - UiState 패턴을 사용하여 로딩, 성공, 에러 상태를 관리
 * - 위치 해석 후 날씨 + 대기질 데이터를 조회하고 병합해 상태로 제공합니다
 */
@HiltViewModel
class WeatherAdditionalViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val effectiveLocationResolver: EffectiveLocationResolver
) : BaseLoadViewModel<WeatherAdditional>() {

    init {
        loadInitial { fetch() }
    }

    /**
     * 데이터 로딩 (위치 → 날씨/대기질 조회 → 병합)
     */
    private suspend fun fetch(): Result<WeatherAdditional> {
        val loc = effectiveLocationResolver.resolve()
        return weatherRepository.getAdditionalWeatherInfo(loc.latitude, loc.longitude)
    }

    /**
     * 날씨 데이터 새로고침 (외부에서 호출 가능)
     */
    fun refreshWeatherData() = load { fetch() }
} 

