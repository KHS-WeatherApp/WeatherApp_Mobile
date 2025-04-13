package com.example.kh_studyprojects_weatherapp.presentation.weather.additional

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
                val result = weatherRepository.getWeatherInfo(
                    latitude = 37.5665, // 서울 위도
                    longitude = 126.9780 // 서울 경도
                )
                
                result.onSuccess { weatherData ->
                    _weatherState.value = weatherData
                }.onFailure { exception ->
                    // 에러 처리
                }
            } catch (e: Exception) {
                // 예외 처리
            }
        }
    }
} 