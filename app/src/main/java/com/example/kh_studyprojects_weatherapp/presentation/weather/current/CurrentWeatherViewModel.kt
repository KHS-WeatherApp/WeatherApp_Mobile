package com.example.kh_studyprojects_weatherapp.presentation.weather.current

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kh_studyprojects_weatherapp.domain.repository.weather.WeatherRepository
import com.example.kh_studyprojects_weatherapp.presentation.location.LocationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CurrentWeatherViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val locationManager: LocationManager
) : ViewModel() {

    // 1. StateFlow를 사용한 데이터 상태 관리(초기값은 빈 Map)
    private val _weatherState = MutableStateFlow<Map<String, Any>>(emptyMap())
    val weatherState: StateFlow<Map<String, Any>> = _weatherState.asStateFlow()

    // 2. 초기화 시점에 데이터 가져오기
    init {
        fetchWeatherData()
    }

    // 3. 데이터 가져오기 함수
    private fun fetchWeatherData() {
        viewModelScope.launch {
            try {
                // 현재 위치 정보 가져오기
                val locationInfo = locationManager.getCurrentLocation()
                
                if (locationInfo != null) {
                    // 위치 정보로 날씨 데이터 가져오기
                    weatherRepository.getWeatherInfo(
                        locationInfo.latitude,
                        locationInfo.longitude
                    ).onSuccess { response ->
                        // 위치 정보 추가
                        val weatherData = response.toMutableMap()
                        weatherData["location"] = locationInfo.address
                        _weatherState.value = weatherData
                    }.onFailure { exception ->
                        println("Error loading weather data: ${exception.message}")
                    }
                } else {
                    println("위치 정보를 가져올 수 없습니다.")
                }
            } catch (e: Exception) {
                println("Exception while loading weather data: ${e.message}")
            }
        }
    }
    
    /**
     * 날씨 데이터 새로고침 (외부에서 호출 가능)
     */
    fun refreshWeatherData() {
        fetchWeatherData()
    }
    
    /**
     * UI 강제 갱신 (외부에서 호출 가능)
     */
    fun forceUIUpdate() {
        // 현재 상태를 다시 방출하여 UI 갱신 트리거
        val currentState = _weatherState.value
        if (currentState.isNotEmpty()) {
            _weatherState.value = currentState.toMutableMap().apply {
                // 강제 갱신을 위한 임시 키 추가
                put("_force_update", System.currentTimeMillis())
            }
            println("CurrentWeatherViewModel: UI 강제 갱신 완료")
        }
    }
} 