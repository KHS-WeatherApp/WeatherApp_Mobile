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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
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
        val weatherCodes = hourlyData["weather_code"] as? List<Int> ?: return emptyList()

        // 현재 시간 구하기
        val now = LocalDateTime.now()
        val currentHourStart = now.withMinute(0).withSecond(0).withNano(0)
        
        // 현재 시간부터의 데이터 인덱스 찾기
        val currentIndex = times.indexOfFirst { time ->
            val dateTime = LocalDateTime.parse(time.replace("Z", ""))
            // 현재 시간대의 데이터부터 포함
            !dateTime.isBefore(currentHourStart)
        }.takeIf { it >= 0 } ?: 0

        // 24시간 데이터를 저장할 리스트
        val result = mutableListOf<WeatherHourlyForecastDto>()
        var hoursCount = 0

        // 현재 시간부터 24시간 동안의 데이터 수집
        while (hoursCount < 24 && (currentIndex + hoursCount) < times.size) {
            val index = currentIndex + hoursCount
            val time = times[index]
            val dateTime = LocalDateTime.parse(time.replace("Z", ""))
            val hourInt = dateTime.hour
            
            val amPm = when (hourInt) {
                in 0..11 -> "오전"
                else -> "오후"
            }

            val formattedHour = "${hourInt}시" // 시간 형식 변환    
            val prob = if (precipitationProbs[index] > 0) "${precipitationProbs[index]}%" else "" // 강수 확률 형식 변환
            val precip = if (precipitations[index] > 0) "${precipitations[index]}mm" else "" // 강수량 형식 변환
            val temp = temperatures[index].toString() // 온도 형식 변환
            val weatherCode = weatherCodes[index].toInt() // 날씨 코드 형식 변환

            result.add(WeatherHourlyForecastDto(amPm, formattedHour, prob, precip, temp, weatherCode))
            hoursCount++
        }

        return result
    }
} 