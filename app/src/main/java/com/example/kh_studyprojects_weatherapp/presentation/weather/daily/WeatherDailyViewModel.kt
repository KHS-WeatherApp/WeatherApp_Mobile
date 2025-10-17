package com.example.kh_studyprojects_weatherapp.presentation.weather.daily

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.kh_studyprojects_weatherapp.data.model.weather.WeatherDailyDto
import com.example.kh_studyprojects_weatherapp.data.model.weather.WeatherMappers
import com.example.kh_studyprojects_weatherapp.data.model.weather.WeatherHourlyForecastDto
import com.example.kh_studyprojects_weatherapp.domain.model.weather.WeatherDailyUiData
import com.example.kh_studyprojects_weatherapp.domain.repository.weather.WeatherRepository
import com.example.kh_studyprojects_weatherapp.presentation.common.location.EffectiveLocationResolver
import com.example.kh_studyprojects_weatherapp.presentation.common.base.BaseLoadViewModel
import kotlinx.coroutines.flow.MutableStateFlow
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
    private val effectiveLocationResolver: EffectiveLocationResolver
) : BaseLoadViewModel<WeatherDailyUiData>() {

    // 펼침 상태 (date 또는 고유 키)
    private val expandedKeys = MutableStateFlow<Set<String>>(emptySet())
    // 전체 날씨 데이터 (어제 포함/15일 여부에 따라 슬라이스됨)
    private var fullWeatherData: List<WeatherDailyDto> = emptyList()
    // 어제 포함/15일 여부 토글 상태
    private var isYesterdayShown = false
    // 15일 여부 토글 상태
    private var is15DaysShown = false

    init {
        loadInitial { fetchByCurrentLocation() }
    }

    // 위치 기반 새로고침
    private suspend fun fetchByCurrentLocation(): Result<WeatherDailyUiData> {
        val loc = effectiveLocationResolver.resolve()
        val locationInfo = "${loc.address}\n위도: ${loc.latitude}, 경도: ${loc.longitude}"
        return fetchWeatherData(loc.latitude, loc.longitude, locationInfo)
    }

    // 위도/경도 기반 새로고침
    private suspend fun fetchWeatherData(
        latitude: Double,
        longitude: Double,
        locationInfo: String
    ): Result<WeatherDailyUiData> {
        Log.i("WeatherVM", "Start fetching weather data: lat=$latitude, lon=$longitude")

        return weatherRepository.getWeatherInfo(latitude, longitude).map { weatherData ->
            Log.i("WeatherVM", "Weather data fetch success")
            fullWeatherData = WeatherMappers.toDailyWeatherDtos(weatherData)

            val target = baseSlice(fullWeatherData).map { item ->
                val key = item.date
                if (key in expandedKeys.value) {
                    item.copy(hourlyForecast = item.hourlyForecast.map { it.copy() })
                } else item
            }

            val currentApiTime = weatherData.current.time ?: ""
            Log.i("WeatherVM", "Current API Time: $currentApiTime")
            Log.i("WeatherVM", "Parsed daily items: ${fullWeatherData.size}")

            WeatherDailyUiData(
                weatherItems = target,
                currentApiTime = currentApiTime,
                locationInfo = locationInfo
            )
        }
    }


    // 어제 포함/15일 토글 반영
    private fun baseSlice(source: List<WeatherDailyDto>): List<WeatherDailyDto> {
        // 어제 포함/15일 토글 반영, subList 대신 drop/take로 '완전히 새 리스트'
        val body = if (is15DaysShown) source.drop(1).take(15) else source.drop(1).take(10)
        return if (isYesterdayShown && source.isNotEmpty()) listOf(source.first()) + body else body
    }

    /**
     * 어제 날씨 토글
     * - UiState를 업데이트하여 Fragment가 최신 데이터를 받도록 합니다
     */
    fun toggleYesterdayWeather() {
        isYesterdayShown = !isYesterdayShown
        updateUiStateWithCurrentData()
    }

    /**
     * 15일 예보 토글
     * - UiState를 업데이트하여 Fragment가 최신 데이터를 받도록 합니다
     */
    fun toggle15DaysWeather() {
        is15DaysShown = !is15DaysShown
        updateUiStateWithCurrentData()
    }

    /**
     * 현재 fullWeatherData를 기반으로 UiState를 업데이트
     * - 토글 상태 변경 시 호출됩니다
     */
    private fun updateUiStateWithCurrentData() {
        viewModelScope.launch {
            // 현재 UiState에서 Success 상태의 데이터를 가져옴
            val currentState = uiState.value
            if (currentState is com.example.kh_studyprojects_weatherapp.presentation.common.base.UiState.Success) {
                val target = baseSlice(fullWeatherData).map { item ->
                    val key = item.date
                    if (key in expandedKeys.value) {
                        item.copy(hourlyForecast = item.hourlyForecast.map { it.copy() })
                    } else item
                }

                // 새로운 WeatherDailyUiData 객체 생성 및 Success 상태로 업데이트
                load {
                    Result.success(
                        WeatherDailyUiData(
                            weatherItems = target,
                            currentApiTime = currentState.data.currentApiTime,
                            locationInfo = currentState.data.locationInfo
                        )
                    )
                }
            }
        }
    }

    /**
     * 미사용: WeatherMappers.toDailyWeatherDtos로 대체됨. 정리 시 삭제 예정.
     */
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
                humidity = "${(humidities.getOrNull(index) as? Number)?.toInt() ?: 0}%",
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

    /**
     * 미사용: WeatherMappers.dayOfWeekKorean 사용으로 대체됨. 정리 시 삭제 예정.
     */
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

    /**
     * 미사용: WeatherMappers.formatDateKorean 사용으로 대체됨. 정리 시 삭제 예정.
     */
    private fun formatDate(dateString: String): String {
        return try {
            val date = LocalDate.parse(dateString)
            "${date.monthValue}.${date.dayOfMonth}"
        } catch (e: Exception) {
            dateString
        }
    }

    /**
     * 펼침/접힘 상태 토글
     * @param key 날짜 키 (예: "2024-01-15")
     * @param expanded 펼침 여부
     */
    fun setExpanded(key: String, expanded: Boolean) {
        expandedKeys.value = expandedKeys.value.toMutableSet().apply {
            if (expanded) add(key) else remove(key)
        }
    }

    /**
     * 날씨 데이터 새로고침 (외부에서 호출 가능)
     */
    fun refreshWeatherData() = load { fetchByCurrentLocation() }
}
