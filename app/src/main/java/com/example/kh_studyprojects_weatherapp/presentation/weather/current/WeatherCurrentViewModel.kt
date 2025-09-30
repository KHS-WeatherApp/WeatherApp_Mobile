package com.example.kh_studyprojects_weatherapp.presentation.weather.current

import com.example.kh_studyprojects_weatherapp.domain.model.weather.WeatherCurrent
import com.example.kh_studyprojects_weatherapp.domain.repository.weather.WeatherRepository
import com.example.kh_studyprojects_weatherapp.presentation.common.base.BaseLoadViewModel
import com.example.kh_studyprojects_weatherapp.presentation.common.location.EffectiveLocationResolver
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * 현재 날씨 화면의 ViewModel
 * - 유효 위치(즐겨찾기 > GPS > 기본값)를 해석하여
 *   현재 날씨 정보를 레포지토리에서 가져와 UI에 제공합니다.
 * - BaseLoadViewModel을 상속해 로딩/성공/에러 상태를 자동 관리합니다.
 */
@HiltViewModel
class WeatherCurrentViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val effectiveLocationResolver: EffectiveLocationResolver
) : BaseLoadViewModel<WeatherCurrent>() {

    init {
        // 최초 진입 시 1회 로딩
        loadInitial { fetch() }
    }

    /**
     * 현재 날씨 데이터 가져오기
     * 1) 유효 위치 계산
     * 2) 레포지토리에서 현재 날씨 조회
     * 3) 응답에 주소 정보를 추가하여 반환
     */
    private suspend fun fetch(): Result<WeatherCurrent> {
        val loc = effectiveLocationResolver.resolve()
        val lat = loc.latitude
        val lon = loc.longitude
        val locationText = loc.address

        return weatherRepository.getCurrentWeather(lat, lon)
            .map { model -> model.copy(location = locationText) }
    }

    /** 외부에서 호출하는 새로고침 진입점 */
    fun refreshWeatherData() = load { fetch() }
}
