package com.example.kh_studyprojects_weatherapp.presentation.weather.current

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kh_studyprojects_weatherapp.domain.repository.weather.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CurrentWeatherViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository
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
                // 서울 좌표 사용
                val latitude = 37.5665
                val longitude = 126.9780
                
                weatherRepository.getWeatherInfo(latitude, longitude)
                    .onSuccess { response ->
                        // 4. 성공시 StateFlow에 데이터 저장
                        _weatherState.value = response
                    }
                    .onFailure { exception ->
                        // 5. 실패시 에러 처리
                        println("Error loading weather data: ${exception.message}")
                    }
            } catch (e: Exception) {
                println("Exception while loading weather data: ${e.message}")
            }
        }
    }
} 