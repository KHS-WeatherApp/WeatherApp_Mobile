package com.example.kh_studyprojects_weatherapp.presentation.weather.current

import androidx.lifecycle.viewModelScope
import com.example.kh_studyprojects_weatherapp.domain.repository.weather.WeatherRepository
import com.example.kh_studyprojects_weatherapp.presentation.common.location.LocationManager
import com.example.kh_studyprojects_weatherapp.presentation.common.base.BaseLoadViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class CurrentWeatherViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val locationManager: LocationManager
) : BaseLoadViewModel() {

    // 1. StateFlow를 사용한 데이터 상태 관리(초기값은 빈 Map)
    private val _weatherState = MutableStateFlow<Map<String, Any>>(emptyMap())
    val weatherState: StateFlow<Map<String, Any>> = _weatherState.asStateFlow()

    // 2. 초기화 시점에 데이터 가져오기
    init {
        loadInitial { fetch() }
    }

    // 3. 데이터 가져오기 함수 (suspend로 분리)
    private suspend fun fetch() {
        val locationInfo = locationManager.getCurrentLocation()
        val (lat, lon, addr) = if (locationInfo != null) {
            Triple(locationInfo.latitude, locationInfo.longitude, locationInfo.address)
        } else {
            Triple(37.5606, 126.986, "기본 위치")
        }
        
        weatherRepository.getWeatherInfo(lat, lon).onSuccess { response ->
            val weatherData = response.toMutableMap()
            weatherData["location"] = addr
            _weatherState.value = weatherData
        }.onFailure { throw it }
    }
    
    /**
     * 날씨 데이터 새로고침 (외부에서 호출 가능)
     */
    fun refreshWeatherData() = load { fetch() }
} 