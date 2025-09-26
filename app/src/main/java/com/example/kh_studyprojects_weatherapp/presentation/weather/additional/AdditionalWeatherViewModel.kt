package com.example.kh_studyprojects_weatherapp.presentation.weather.additional

import android.util.Log
import com.example.kh_studyprojects_weatherapp.data.model.weather.WeatherMappers
import com.example.kh_studyprojects_weatherapp.domain.repository.weather.WeatherRepository
import com.example.kh_studyprojects_weatherapp.presentation.common.base.BaseLoadViewModel
import com.example.kh_studyprojects_weatherapp.presentation.common.location.EffectiveLocationResolver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
/**
 * 추가 정보(대기질/UV 등) 화면의 ViewModel
 * - 위치 해석 후 날씨 + 대기질 데이터를 조회하고 병합해 상태로 제공합니다.
 */
class AdditionalWeatherViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val effectiveLocationResolver: EffectiveLocationResolver
) : BaseLoadViewModel() {

    // 병합된 날씨 + 대기질 데이터 상태
    private val _weatherState = MutableStateFlow<Map<String, Any>>(emptyMap())
    val weatherState: StateFlow<Map<String, Any>> = _weatherState

    init {
        // 최초 진입 시 1회 로딩
        loadInitial { fetch() }
    }

    /** 데이터 로딩(위치 → 날씨/대기질 조회 → 병합) */
    private suspend fun fetch() {
        // 위치 결정(즐겨찾기 > GPS > 기본값)
        val loc = effectiveLocationResolver.resolve()
        // 날씨 + 대기질 동시 요청
        val weatherResult = weatherRepository.getWeatherInfo(loc.latitude, loc.longitude)
        // 대기질은 없을 수도 있으므로 실패해도 무시
        val airPollutionResult = weatherRepository.getAdditionalWeatherInfo(loc.latitude, loc.longitude)
        // 결과 병합
        var weatherData: Map<String, Any>? = null
        weatherResult.onSuccess { data -> weatherData = data }
            .onFailure { e -> Log.e("AdditionalWeather", "기본 날씨 로드 실패: ${e.message}") }
        // 대기질은 실패해도 무시(없을 수 있음)
        var airData: Map<String, Any>? = null
        airPollutionResult.onSuccess { data -> airData = data }
            .onFailure { e -> Log.e("AdditionalWeather", "대기질 로드 실패: ${e.message}") }
        // 둘 다 성공했을 때만 병합
        val merged = WeatherMappers.mergeWeatherAndAir(weatherData, airData)
        if (merged.isNotEmpty()) {
            _weatherState.value = merged
        } else {
            throw Exception("데이터를 불러오지 못했습니다.")
        }
    }
    
    /**
     * 날씨 데이터 새로고침 (외부에서 호출 가능)
     */
    /** 외부에서 호출되는 새로고침 */
    fun refreshWeatherData() = load { fetch() }
} 
