package com.example.kh_studyprojects_weatherapp.presentation.weather.daily

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kh_studyprojects_weatherapp.data.model.weather.WeatherDailyDto
import com.example.kh_studyprojects_weatherapp.data.model.weather.WeatherHourlyForecastDto
import com.example.kh_studyprojects_weatherapp.domain.repository.weather.WeatherRepository
import com.example.kh_studyprojects_weatherapp.presentation.location.LocationManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel

@HiltViewModel
class WeatherDailyViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val locationManager: LocationManager
) : ViewModel() {

    private val _weatherItems = MutableStateFlow<List<WeatherDailyDto>>(emptyList())
    val weatherItems: StateFlow<List<WeatherDailyDto>> = _weatherItems.asStateFlow()

    private val _currentApiTime = MutableStateFlow<String?>(null)
    val currentApiTime: StateFlow<String?> = _currentApiTime.asStateFlow()

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private var fullWeatherData: List<WeatherDailyDto> = emptyList()
    private var isYesterdayShown = false
    private var is15DaysShown = false

    private val _locationInfo = MutableStateFlow<String?>(null)
    val locationInfo: StateFlow<String?> = _locationInfo.asStateFlow()

    init {
        viewModelScope.launch {
            val locationInfo = locationManager.getCurrentLocation()

            if (locationInfo != null) {
                val currentLatitude = locationInfo.latitude
                val currentLongitude = locationInfo.longitude
                fetchWeatherData(currentLatitude, currentLongitude)
            } else {
                fetchWeatherData(37.5606, 126.986) // 기본 위치
            }
        }
    }

    fun fetchWeatherData(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                Log.i("WeatherVM", "Start fetching weather data: lat=$latitude, lon=$longitude")
                weatherRepository.getWeatherInfo(latitude, longitude)
                    .onSuccess { response ->
                        Log.i("WeatherVM", "Weather data fetch success")
                        val data = response as? Map<String, Any>
                        if (data != null) {
                            fullWeatherData = convertToWeatherDailyItems(data)
                            _weatherItems.value = fullWeatherData.subList(1, minOf(11, fullWeatherData.size))
                            val daily = data["current"] as? Map<String, Any> ?: emptyMap();
                            _currentApiTime.value = daily["time"] as? String;
                            Log.i("WeatherVM", "Current API Time: ${_currentApiTime.value}")
                            _error.value = null
                            Log.i("WeatherVM", "Parsed daily items: ${fullWeatherData.size}")
                        } else {
                            _error.value = "날씨 데이터 형식이 올바르지 않습니다"
                            _weatherItems.value = emptyList()
                            Log.e("WeatherVM", "Invalid data format")
                        }
                    }
                    .onFailure { exception ->
                        _error.value = exception.message ?: "알 수 없는 오류가 발생했습니다"
                        _weatherItems.value = emptyList()
                        Log.e("WeatherVM", "Weather data fetch failed", exception)
                    }
            } catch (e: Exception) {
                _error.value = e.message
                _weatherItems.value = emptyList()
                Log.e("WeatherVM", "Exception in fetchWeatherData", e)
            } finally {
                _isLoading.value = false
                Log.i("WeatherVM", "Fetching weather data done")
            }
        }
    }


    fun toggleYesterdayWeather() {
        val currentData = _weatherItems.value
        if (fullWeatherData.isEmpty() || fullWeatherData.firstOrNull() == null) return

        isYesterdayShown = !isYesterdayShown

        _weatherItems.value = if (isYesterdayShown) {
            listOf(fullWeatherData[0]) + currentData
        } else {
            currentData.filter { it.type != WeatherDailyDto.Type.YESTERDAY }
        }
    }

    fun toggle15DaysWeather() {
        if (fullWeatherData.isEmpty()) return

        is15DaysShown = !is15DaysShown

        _weatherItems.value = if (is15DaysShown) {
            fullWeatherData.subList(1, minOf(16, fullWeatherData.size))
        } else {
            fullWeatherData.subList(1, minOf(11, fullWeatherData.size))
        }
    }

    private fun convertToWeatherDailyItems(data: Map<String, Any>): List<WeatherDailyDto> {
        val daily = data["daily"] as? Map<*, *> ?: return emptyList()
        val hourly = data["hourly"] as? Map<*, *> ?: return emptyList()

        val dailyTime = daily["time"] as? List<String> ?: return emptyList()
        val maxTemps = daily["temperature_2m_max"] as? List<Double> ?: return emptyList()
        val minTemps = daily["temperature_2m_min"] as? List<Double> ?: return emptyList()
        val weatherCodes = daily["weather_code"] as? List<*> ?: return emptyList()
        val precipitations = daily["precipitation_sum"] as? List<Double> ?: return emptyList()
        val humidities = daily["precipitation_probability_max"] as? List<*> ?: return emptyList()
        val apparentTempMaxs = daily["apparent_temperature_min"] as? List<Double> ?: return emptyList()
        val apparentTempMins = daily["apparent_temperature_max"] as? List<Double> ?: return emptyList()

        val hourlyTimes = hourly["time"] as? List<String> ?: return emptyList()
        val hourlyTemps = hourly["temperature_2m"] as? List<Double> ?: return emptyList()
        val hourlyPrecip = hourly["precipitation"] as? List<Double> ?: return emptyList()
        //val hourlyProb = hourly["precipitation_probability"] as? List<Int> ?: return emptyList()
        //val hourlyCodes = hourly["weather_code"] as? List<Int> ?: return emptyList()
        val hourlyProbRaw = hourly["precipitation_probability"] as? List<*> ?: return emptyList()
        val hourlyCodesRaw = hourly["weather_code"] as? List<*> ?: return emptyList()
        val hourlyApparentTemps = hourly["apparent_temperature"] as? List<Double> ?: return emptyList()

        val hourlyProb = hourlyProbRaw.map { (it as Number).toInt() }
        val hourlyCodes = hourlyCodesRaw.map { (it as Number).toInt() }

        val hourlyPerDay = hourlyTimes.groupBy { it.substring(0, 10) } // yyyy-MM-dd

        val lowestTemp = (minTemps.minOrNull() ?: -18.0)
        val highestTemp = (maxTemps.maxOrNull() ?: 38.0)

        return dailyTime.mapIndexed { index, date ->

            val hourlyDataList = hourlyPerDay[date]

            if (hourlyDataList == null) {
                Log.w("TodayViewHolder", "No hourly data for date: $date")
            } else {
                Log.i("TodayViewHolder", "Found hourly data (${hourlyDataList.size}) for date: $date")
            }

            val hourlyForecast = hourlyDataList?.mapNotNull { timeStr ->
                try {
                    val idx = hourlyTimes.indexOf(timeStr)

                    val hour = LocalDateTime.parse(timeStr, DateTimeFormatter.ISO_DATE_TIME).hour
                    WeatherHourlyForecastDto(
                        tvAmPm = if (hour < 12) "오전" else "오후",
                        tvHour = if (hour == 0 || hour == 12 ) "12" else (hour % 12).toString(),
                        probability = "${hourlyProb.getOrNull(idx) ?: 0}%",
                        precipitation = "${hourlyPrecip.getOrNull(idx) ?: 0.0}mm",
                        temperature = "${hourlyTemps.getOrNull(idx) ?: 0.0}°",
                        weatherCode = hourlyCodes.getOrNull(idx) ?: 0,
                        apparent_temperature = "${hourlyApparentTemps.getOrNull(idx) ?: 0.0}°",
                    )
                } catch (e: Exception) {
                    Log.e("TodayViewHolder", "hourlyForecast error: $timeStr", e)
                    null
                }
            } ?: emptyList()

            Log.i("TodayViewHolder", "hourlyForecast size: ${hourlyForecast.size}")
            hourlyForecast.forEachIndexed { i, it ->
                Log.i("TodayViewHolder", "[$i] ${it.tvAmPm} ${it.tvHour}시 / ${it.temperature} / ${it.probability} / ${it.precipitation} / code=${it.weatherCode}")
            }

            WeatherDailyDto(
                type = when (index) {
                    0 -> WeatherDailyDto.Type.YESTERDAY
                    1 -> WeatherDailyDto.Type.TODAY
                    else -> WeatherDailyDto.Type.OTHER
                },
                week = if (index == 0) "어제" else if (index == 1) "오늘" else getDayOfWeek(date),
                date = if (index == 0 || index == 1) date else formatDate(date),
                precipitation = "${precipitations.getOrNull(index) ?: 0.0}mm",
                humidity = "${(humidities?.get(index) as? Number)?.toInt() ?: 0}%",
                minTemp = "${minTemps.getOrNull(index) ?: 0.0}°",
                maxTemp = "${maxTemps.getOrNull(index) ?: 0.0}°",
                weatherCode = (weatherCodes.getOrNull(index) as? Number)?.toInt() ?: 0,
                isVisible = true,
                globalMinTemp = lowestTemp,
                globalMaxTemp = highestTemp,
                hourlyForecast = hourlyForecast,
                apparent_temperature_max = "${apparentTempMaxs.getOrNull(index) ?: 0.0}°",
                apparent_temperature_min = "${apparentTempMins.getOrNull(index) ?: 0.0}°",
            )
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
    
    /**
     * 날씨 데이터 새로고침 (외부에서 호출 가능)
     */
    fun refreshWeatherData() {
        viewModelScope.launch {
            val locationInfo = locationManager.getCurrentLocation()
            if (locationInfo != null) {
                fetchWeatherData(locationInfo.latitude, locationInfo.longitude)
            } else {
                fetchWeatherData(37.5606, 126.986) // 기본 위치
            }
        }
    }
    
    /**
     * UI 강제 갱신 (외부에서 호출 가능)
     */
    fun forceUIUpdate() {
        // 현재 상태를 다시 방출하여 UI 갱신 트리거
        val currentItems = _weatherItems.value
        val currentApiTime = _currentApiTime.value
        
        if (currentItems.isNotEmpty()) {
            _weatherItems.value = currentItems.toMutableList().apply {
                // 강제 갱신을 위한 임시 데이터 추가
                add(0, currentItems.first().copy(
                    date = "${currentItems.first().date} (갱신됨)"
                ))
                removeAt(0)
            }
            println("WeatherDailyViewModel: UI 강제 갱신 완료")
        }
        
        if (currentApiTime != null) {
            _currentApiTime.value = "${currentApiTime}_updated"
            _currentApiTime.value = currentApiTime
        }
    }
}