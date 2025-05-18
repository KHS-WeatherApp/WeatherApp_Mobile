package com.example.kh_studyprojects_weatherapp.presentation.weather.additional

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kh_studyprojects_weatherapp.domain.repository.weather.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdditionalWeatherViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository
) : ViewModel() {

    private val _weatherState = MutableStateFlow<Map<String, Any>>(emptyMap())
    val weatherState: StateFlow<Map<String, Any>> = _weatherState

    init {
        fetchWeatherData()
    }

    private fun fetchWeatherData() {
        viewModelScope.launch {
            try {
                // 1. 기본 날씨 데이터 가져오기
                val weatherResult = weatherRepository.getWeatherInfo(
                    latitude = 37.5665, // 서울 위도
                    longitude = 126.9780 // 서울 경도
                )

                // 2. 대기질 데이터 가져오기
                val airPollutionResult = weatherRepository.getAdditionalWeatherInfo(
                    latitude = 37.5665,
                    longitude = 126.9780
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
                    Log.e("AdditionalWeather", "데이터를 가져오지 못했습니다.")
                }
            } catch (e: Exception) {
                Log.e("AdditionalWeather", "날씨 데이터 로드 중 예외 발생: ${e.message}")
            }
        }
    }
} 