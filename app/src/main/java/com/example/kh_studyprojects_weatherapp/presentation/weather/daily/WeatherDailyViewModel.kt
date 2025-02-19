package com.example.kh_studyprojects_weatherapp.presentation.weather.daily

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kh_studyprojects_weatherapp.domain.model.weather.WeatherDailyDto
import com.example.kh_studyprojects_weatherapp.domain.repository.weather.WeatherRepository
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import kotlin.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class WeatherDailyViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository
) : ViewModel() {

    private val _weatherItems = MutableStateFlow<List<WeatherDailyDto>>(emptyList())
    val weatherItems: StateFlow<List<WeatherDailyDto>> = _weatherItems.asStateFlow()

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private var fullWeatherData: List<WeatherDailyDto> = emptyList()
    private var isYesterdayShown = false
    private var is15DaysShown = false

    init {
        fetchWeatherData(37.5665, 126.9780) // 초기 데이터 로드 (서울 좌표)
    }

    fun fetchWeatherData(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                weatherRepository.getWeatherInfo(latitude, longitude)
                    .onSuccess { response ->
                        val data = response as? Map<String, Any>
                        if (data != null) {
                            fullWeatherData = convertToWeatherDailyItems(data)
                            // 기본적으로 오늘부터 10일치 데이터만 보여주기
                            _weatherItems.value = fullWeatherData.subList(1, minOf(11, fullWeatherData.size))
                            _error.value = null
                        } else {
                            _error.value = "날씨 데이터 형식이 올바르지 않습니다"
                            _weatherItems.value = emptyList()
                        }
                    }
                    .onFailure { exception ->
                        _error.value = exception.message ?: "알 수 없는 오류가 발생했습니다"
                        _weatherItems.value = emptyList()
                    }
            } catch (e: Exception) {
                _error.value = e.message
                _weatherItems.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleYesterdayWeather() {
        val currentData = _weatherItems.value
        isYesterdayShown = !isYesterdayShown

        _weatherItems.value = if (isYesterdayShown) {
            // 어제 데이터를 추가
            listOf(fullWeatherData[0]) + currentData
        } else {
            // 어제 데이터 제거
            currentData.filter { it.type != WeatherDailyDto.Type.YESTERDAY }
        }
    }

    fun toggle15DaysWeather() {
        is15DaysShown = !is15DaysShown

        _weatherItems.value = if (is15DaysShown) {
            // 15일치 데이터 보여주기 (어제 데이터 제외)
            fullWeatherData.subList(1, minOf(16, fullWeatherData.size))
        } else {
            // 기본 10일로 돌아가기
            fullWeatherData.subList(1, minOf(11, fullWeatherData.size))
        }
    }

    private fun convertToWeatherDailyItems(weatherData: Map<String, Any>): List<WeatherDailyDto> {
        return try {
            // daily 데이터 가져오기
            val dailyData = weatherData["daily"] as? Map<*, *>
            if (dailyData == null) {
                println("Daily data is null") // 디버깅용
                return emptyList()
            }

            // 필요한 데이터 추출
            val dates = dailyData["time"] as? List<*>
            val maxTemps = dailyData["temperature_2m_max"] as? List<*>
            val minTemps = dailyData["temperature_2m_min"] as? List<*>
            val weatherCodes = dailyData["weather_code"] as? List<*>
            val precipitations = dailyData["precipitation_sum"] as? List<*>
            val humidities = dailyData["precipitation_probability_max"] as? List<*>

            // null 체크
            if (dates == null || maxTemps == null || minTemps == null || 
                weatherCodes == null || precipitations == null || humidities == null) {
                println("Some required data is null") // 디버깅용
                return emptyList()
            }

            buildList {
                // 어제 날씨
                add(WeatherDailyDto(
                    type = WeatherDailyDto.Type.YESTERDAY,
                    week = "어제",
                    date = dates[0].toString(),
                    precipitation = "${precipitations[0]}mm",
                    humidity = "${humidities[0]}%",
                    minTemp = "${minTemps[0]}°",
                    maxTemp = "${maxTemps[0]}°",
                    weatherCode = (weatherCodes[0] as? Number)?.toInt() ?: 0,
                    isVisible = true
                ))

                // 오늘 날씨
                add(WeatherDailyDto(
                    type = WeatherDailyDto.Type.TODAY,
                    week = "오늘",
                    date = dates[1].toString(),
                    precipitation = "${precipitations[1]}mm",
                    humidity = "${humidities[1]}%",
                    minTemp = "${minTemps[1]}°",
                    maxTemp = "${maxTemps[1]}°",
                    weatherCode = (weatherCodes[1] as? Number)?.toInt() ?: 0,
                    isVisible = true
                ))

                // 다음 날씨들
                for (i in 2 until minOf(dates.size, 16)) {
                    add(WeatherDailyDto(
                        type = WeatherDailyDto.Type.OTHER,
                        week = getDayOfWeek(dates[i].toString()),
                        date = formatDate(dates[i].toString()),
                        precipitation = "${precipitations[i]}mm",
                        humidity = "${humidities[i]}%",
                        minTemp = "${minTemps[i]}°",
                        maxTemp = "${maxTemps[i]}°",
                        weatherCode = (weatherCodes[i] as? Number)?.toInt() ?: 0,
                        isVisible = true
                    ))
                }
            }
        } catch (e: Exception) {
            println("Conversion error: ${e.message}") // 디버깅용
            e.printStackTrace()
            emptyList()
        }
    }

    private fun getDayOfWeek(dateString: String): String {
        return try {
            val date = LocalDate.parse(dateString)
            when (date.dayOfWeek) {
                DayOfWeek.MONDAY -> "월"
                DayOfWeek.TUESDAY -> "화"
                DayOfWeek.WEDNESDAY -> "수"
                DayOfWeek.THURSDAY -> "목"
                DayOfWeek.FRIDAY -> "금"
                DayOfWeek.SATURDAY -> "토"
                DayOfWeek.SUNDAY -> "일"
            }
        } catch (e: Exception) {
            "?"
        }
    }

    private fun formatDate(dateString: String): String {
        return try {
            val date = LocalDate.parse(dateString)
            "${date.monthValue}.${date.dayOfMonth}"
        } catch (e: Exception) {
            dateString
        }
    }

    fun errorShown() {
        _error.value = null
    }
} 