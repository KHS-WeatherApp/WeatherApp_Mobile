package com.example.kh_studyprojects_weatherapp.presentation.weather.additional

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.kh_studyprojects_weatherapp.domain.repository.weather.WeatherRepository
import com.example.kh_studyprojects_weatherapp.presentation.common.base.BaseLoadViewModel
import com.example.kh_studyprojects_weatherapp.presentation.common.location.EffectiveLocationResolver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class AdditionalWeatherViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val effectiveLocationResolver: EffectiveLocationResolver
) : BaseLoadViewModel() {

    private val _weatherState = MutableStateFlow<Map<String, Any>>(emptyMap())
    val weatherState: StateFlow<Map<String, Any>> = _weatherState

    init {
        loadInitial { fetch() }
    }

    private suspend fun fetch() {
        // 위치 결정(즐겨찾기 > GPS > 기본값)
        val loc = effectiveLocationResolver.resolve()
        // 1. 기본 날씨 데이터 가져오기
        val weatherResult = weatherRepository.getWeatherInfo(
            latitude = loc.latitude,
            longitude = loc.longitude
        )

        // 2. 대기질 데이터 가져오기
        val airPollutionResult = weatherRepository.getAdditionalWeatherInfo(
            latitude = loc.latitude,
            longitude = loc.longitude
        )

        // 3. 두 결과를 합치기
        val combinedData = mutableMapOf<String, Any>()
        
        weatherResult.onSuccess { weatherData ->
            combinedData.putAll(weatherData)
        }.onFailure { exception ->
            Log.e("AdditionalWeather", "기본 날씨 데이터 로드 실패: ${exception.message}")
        }

        airPollutionResult.onSuccess { airData ->
            combinedData.putAll(airData)
        }.onFailure { exception ->
            Log.e("AdditionalWeather", "대기질 데이터 로드 실패: ${exception.message}")
        }

        // 4. 합쳐진 데이터를 상태에 저장
        if (combinedData.isNotEmpty()) {
            _weatherState.value = combinedData
        } else {
            throw Exception("데이터를 가져오지 못했습니다.")
        }
    }
    
    /**
     * 날씨 데이터 새로고침 (외부에서 호출 가능)
     */
    fun refreshWeatherData() = load { fetch() }
} 