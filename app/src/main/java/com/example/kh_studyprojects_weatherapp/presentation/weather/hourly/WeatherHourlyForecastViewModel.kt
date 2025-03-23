package com.example.kh_studyprojects_weatherapp.presentation.weather.hourly

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kh_studyprojects_weatherapp.domain.model.weather.WeatherHourlyForecastDto
import com.example.kh_studyprojects_weatherapp.domain.repository.weather.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeatherHourlyForecastViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository
) : ViewModel() {

    private val _hourlyForecastItems = MutableStateFlow<List<WeatherHourlyForecastDto>>(emptyList())
    val hourlyForecastItems: StateFlow<List<WeatherHourlyForecastDto>> = _hourlyForecastItems.asStateFlow()

    fun fetchHourlyForecast() {
        viewModelScope.launch {
            try {
                val result = weatherRepository.getWeatherInfo(37.5665, 126.9780) // 서울 좌표
                result.onSuccess { response ->
                    // API 응답을 WeatherHourlyForecastDto 리스트로 변환
                    val hourlyData = response["hourly"] as? Map<String, Any>
                    if (hourlyData != null) {
                        val items = convertToHourlyForecast(hourlyData)
                        _hourlyForecastItems.value = items
                    }
                }.onFailure { e ->
                    e.printStackTrace()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun convertToHourlyForecast(hourlyData: Map<String, Any>): List<WeatherHourlyForecastDto> {
        val times = hourlyData["time"] as? List<String> ?: return emptyList()
        val temperatures = hourlyData["temperature_2m"] as? List<Double> ?: return emptyList()
        val precipitationProbs = hourlyData["precipitation_probability"] as? List<Int> ?: return emptyList()
        val precipitations = hourlyData["precipitation"] as? List<Double> ?: return emptyList()

        // 현재 시간 구하기
        val now = java.time.LocalDateTime.now()
        val currentHour = now.hour
        
        // 24시간 데이터를 저장할 리스트
        val result = mutableListOf<WeatherHourlyForecastDto>()
        var hoursCount = 0
        var currentIndex = times.indexOfFirst { time ->
            val hour = time.split("T")[1].substring(0, 2).toInt()
            hour == currentHour
        }

        // 24시간 동안의 데이터 수집
        while (hoursCount < 24 && currentIndex < times.size) {
            val time = times[currentIndex]
            val hourInt = time.split("T")[1].substring(0, 2).toInt()
            
            val amPm = when (hourInt) {
                in 0..11 -> "오전"
                else -> "오후"
            }

            val formattedHour = "${hourInt}시"
            val prob = if (precipitationProbs[currentIndex] > 0) "${precipitationProbs[currentIndex]}%" else ""
            val precip = if (precipitations[currentIndex] > 0) "${precipitations[currentIndex]}mm" else ""
            val temp = temperatures[currentIndex].toString()

            result.add(WeatherHourlyForecastDto(amPm, formattedHour, prob, precip, temp))

            currentIndex = (currentIndex + 1) % times.size
            hoursCount++
        }

        return result
    }
} 