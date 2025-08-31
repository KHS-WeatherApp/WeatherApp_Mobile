package com.example.kh_studyprojects_weatherapp.presentation.weather.hourly

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.kh_studyprojects_weatherapp.data.model.weather.WeatherHourlyForecastDto
import com.example.kh_studyprojects_weatherapp.domain.repository.weather.WeatherRepository
import com.example.kh_studyprojects_weatherapp.presentation.common.location.LocationManager
import com.example.kh_studyprojects_weatherapp.presentation.common.base.BaseLoadViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class WeatherHourlyForecastViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val locationManager: LocationManager
) : BaseLoadViewModel() {

    private val _hourlyForecastItems = MutableStateFlow<List<WeatherHourlyForecastDto>>(emptyList())
    val hourlyForecastItems: StateFlow<List<WeatherHourlyForecastDto>> = _hourlyForecastItems.asStateFlow()




    private val _locationInfo = MutableStateFlow<String?>(null)
    val locationInfo: StateFlow<String?> = _locationInfo.asStateFlow()

    // 현재 위치의 위도와 경도를 저장하는 변수
    private var currentLatitude: Double = -28.1662 // 기본값 좌표 설정
    private var currentLongitude: Double = 29.1732

    init {
        loadInitial { fetch() }
    }

    private suspend fun fetch() {
        Log.d("WeatherHourlyForecast", "위치 정보 요청 시작")
        
        // 현재 위치 가져오기
        val locationInfo = locationManager.getCurrentLocation()
        if (locationInfo != null) {
            currentLatitude = locationInfo.latitude
            currentLongitude = locationInfo.longitude
            _locationInfo.value = "${locationInfo.address}\n위도: $currentLatitude, 경도: $currentLongitude"
            Log.d("WeatherHourlyForecast", "GPS 위치 정보 획득 성공 - ${locationInfo.address}")
        } else {
            _locationInfo.value = "기본 위치(서울)\n위도: $currentLatitude, 경도: $currentLongitude"
            Log.w("WeatherHourlyForecast", "GPS 위치 정보 획득 실패 - 기본값(서울) 사용")
        }
        
        // 가져온 위치 정보로 날씨 데이터 요청
        Log.d("WeatherHourlyForecast", "날씨 정보 요청 시작 - 위도: $currentLatitude, 경도: $currentLongitude")
        val result = weatherRepository.getWeatherInfo(currentLatitude, currentLongitude)
        result.onSuccess { response ->
            val hourlyData = response["hourly"] as? Map<String, Any>
            if (hourlyData != null) {
                val items = convertToHourlyForecast(hourlyData)
                _hourlyForecastItems.value = items
                Log.d("WeatherHourlyForecast", "날씨 정보 획득 성공 - ${items.size}개의 시간대 데이터")
            } else {
                throw Exception("날씨 데이터 형식이 올바르지 않습니다.")
            }
        }.onFailure { e ->
            throw e
        }
    }

    /**
     * 날씨 데이터 새로고침 (외부에서 호출 가능)
     */
    fun refreshWeatherData() = load { fetch() }

    private fun convertToHourlyForecast(hourlyData: Map<String, Any>): List<WeatherHourlyForecastDto> {
        val times = hourlyData["time"] as? List<String> ?: return emptyList() // 시간 데이터
        val temperatures = hourlyData["temperature_2m"] as? List<Double> ?: return emptyList() // 온도 데이터
        val precipitationProbs = hourlyData["precipitation_probability"] as? List<Int> ?: return emptyList() // 강수 확률 데이터
        val precipitations = hourlyData["precipitation"] as? List<Double> ?: return emptyList() // 강수량 데이터
        val weatherCodes = hourlyData["weather_code"] as? List<Int> ?: return emptyList() // 날씨 코드 데이터
        val apparentTemps = hourlyData["apparent_temperature"] as? List<Double> ?: return emptyList() // 체감온도 데이터
        
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
            val temp = "${temperatures[index]}" // 온도 형식 변환
            val weatherCode = weatherCodes[index].toInt() // 날씨 코드 형식 변환
            val apparentTemp =  "${apparentTemps[index]}" // 체감온도 형식 변환

            result.add(WeatherHourlyForecastDto(amPm, formattedHour, prob, precip, temp, weatherCode , apparentTemp))
            hoursCount++
        }

        return result
    }
} 