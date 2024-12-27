package com.example.kh_studyprojects_weatherapp

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kh_studyprojects_weatherapp.api.WeatherRepository
import com.example.kh_studyprojects_weatherapp.data.WeatherData
import com.example.kh_studyprojects_weatherapp.type.ViewType
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class WeatherViewModel : ViewModel() {

    private val _weatherData = MutableLiveData<List<WeatherData>>()
    val weatherData: LiveData<List<WeatherData>> get() = _weatherData

    private var fullWeatherData: List<WeatherData> = emptyList()
    private val repository = WeatherRepository()

    // 어제 데이터를 토글하는 변수
    private var isYesterdayShown = false

    // 추가 5일 데이터를 토글하는 변수
    private var is15DaysShown = false

    private val TAG = "WeatherViewModel"

    fun fetchWeatherData(params: Map<String, Any>) {
        Log.d(TAG, "fetchWeatherData called with params: $params") // 함수 호출 로그

        viewModelScope.launch {
            try {
                repository.getWeatherData(
                    params,
                    onSuccess = { rawData ->
                        Log.d(TAG, "API call succeeded with rawData: $rawData") // 성공 로그
                        fullWeatherData = processWeatherData(rawData)
                        Log.d(TAG, "Processed weather data: $fullWeatherData") // 데이터 처리 로그

                        // 기본적으로 오늘부터 10일치 데이터 보여주기
                        val subList = fullWeatherData.subList(1, 11)
                        _weatherData.postValue(subList)
                        Log.d(TAG, "Weather data posted: $subList") // 데이터 포스트 로그
                    },
                    onFailure = { error ->
                        Log.e(TAG, "API call failed with error: ${error.message}", error) // 실패 로그
                        // 에러 처리 로직 추가
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Exception in fetchWeatherData: ${e.message}", e) // 예외 로그
            }
        }
    }

    fun showYesterdayWeather() {
        val currentData = _weatherData.value ?: emptyList()
        isYesterdayShown = !isYesterdayShown

        if (isYesterdayShown) {
            // 어제 데이터를 추가하여 보여줌
            val yesterdayData = fullWeatherData.take(1) // 어제 데이터만 추출
            _weatherData.postValue(yesterdayData + currentData)
        } else {
            // 어제 데이터를 제거하고 원래 데이터로 돌아감
            _weatherData.postValue(currentData.drop(1))
        }
    }

    fun toggle15DaysWeather() {
        val currentData = _weatherData.value ?: emptyList()
        is15DaysShown = !is15DaysShown

        if (is15DaysShown) {
            // 추가 5일 데이터를 보여줌 (오늘부터 10일 이후 데이터)
            val additionalData = fullWeatherData.subList(11, fullWeatherData.size)
            _weatherData.postValue(currentData + additionalData)
        } else {
            // 추가 데이터를 제거하고 기본 10일로 돌아가기
            _weatherData.postValue(fullWeatherData.subList(1, 11))
        }
    }

    private fun processWeatherData(rawData: Map<String, Any>): List<WeatherData> {
        val daily = rawData["daily"] as? Map<String, Any>
        val time = daily?.get("time") as? List<String>
        val maxTemp = daily?.get("temperature_2m_max") as? List<Double>
        val minTemp = daily?.get("temperature_2m_min") as? List<Double>
        val precipitation = daily?.get("precipitation_sum") as? List<Double>
        val humidity = daily?.get("precipitation_probability_max") as? List<Double>
        val weatherCode = daily?.get("weather_code") as? List<Double>

        return time?.mapIndexed { index, date ->
            val week = SimpleDateFormat("EEEE", Locale("ko", "KR")).format(SimpleDateFormat("yyyy-MM-dd").parse(date))
            val displayDate = SimpleDateFormat("MM-dd").format(SimpleDateFormat("yyyy-MM-dd").parse(date))
            val weatherEmoji = getWeatherEmoji(weatherCode?.getOrNull(index)?.toInt())

            WeatherData(
                week = week,
                date = displayDate,
                precipitation = "${precipitation?.getOrNull(index) ?: 0.0} mm",
                humidity = "${humidity?.getOrNull(index)?.toInt() ?: 0}%",
                minTemp = "${minTemp?.getOrNull(index)}°",
                maxTemp = "${maxTemp?.getOrNull(index)}°",
                weatherEmoji = weatherEmoji,
                type = ViewType.fromOrdinal(index)
            )
        } ?: emptyList()
    }

    // 날씨 코드에 따른 이모지 함수
    private fun getWeatherEmoji(weatherCode: Int?): String {
        return when (weatherCode) {
            0 -> "\uD83C\uDF1E" // ☀️ Clear sky
            1, 2, 3 -> "\u26C5" // 🌤 Mainly clear, partly cloudy, overcast
            45, 48 -> "\uD83C\uDF2B" // 🌫 Fog and depositing rime fog
            51, 53, 55 -> "\u2614" // 🌧 Drizzle
            61, 63, 65 -> "\uD83C\uDF27" // 🌧 Rain
            71, 73, 75 -> "\uD83C\uDF28" // 🌨 Snowfall
            80, 81, 82 -> "\uD83C\uDF27" // 🌧 Rain showers
            95, 96, 99 -> "\u26A1" // 🌩 Thunderstorm
            else -> "\u2753" // ❓ Unknown
        }
    }
}
