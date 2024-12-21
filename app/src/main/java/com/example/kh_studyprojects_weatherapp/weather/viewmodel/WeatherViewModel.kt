package com.example.kh_studyprojects_weatherapp.weather.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kh_studyprojects_weatherapp.weather.api.WeatherRepository
import kotlinx.coroutines.launch
import  com.example.kh_studyprojects_weatherapp.weather.api.WeatherResponse

class WeatherViewModel : ViewModel() {
    private val weatherRepository = WeatherRepository()

    private val _weatherState = MutableLiveData<WeatherState>()
    val weatherState: LiveData<WeatherState> = _weatherState

    fun fetchWeatherData() {
        viewModelScope.launch {
            try {
                val result = weatherRepository.getWeatherInfo(
                    latitude = 37.5606,
                    longitude = 126.9433
                )
                result.onSuccess { response: Map<String, Any> ->
                    Log.d("WeatherViewModel", "날씨 데이터: $response")

                    // Map에서 데이터 추출
                    val current = response["current"] as Map<String, Any>
                    val daily = response["daily"] as Map<String, Any>

                    val currentTemp = current["temperature_2m"] as Double
                    val apparentTemp = current["apparent_temperature"] as Double

                    val tempMin = (daily["temperature_2m_min"] as List<Double>)[1]
                    val tempMax = (daily["temperature_2m_max"] as List<Double>)[1]

                    Log.e("myodong","current: ${current}")
                    Log.e("myodong","daily: ${daily}")
                    Log.e("myodong","currentTemp: ${currentTemp}")
                    Log.e("myodong","apparentTemp: ${apparentTemp}")
                    Log.e("myodong","tempMin: ${tempMin}")
                    Log.e("myodong","tempMax: ${tempMax}")

                    _weatherState.value = WeatherState.Success(
                        WeatherData(
                            currentTemperature = currentTemp,
                            minTemperature = tempMin,
                            maxTemperature = tempMax,
                            apparentTemperature = apparentTemp
                        )
                    )
                }.onFailure { exception ->
                    Log.e("WeatherViewModel", "날씨 데이터 가져오기 실패", exception)
                    _weatherState.value = WeatherState.Error("날씨 정보를 가져오는데 실패했습니다: ${exception.message}")
                }
            } catch (e: Exception) {
                Log.e("WeatherViewModel", "API 호출 실패", e)
                _weatherState.value = WeatherState.Error("서버 연결에 실패했습니다: ${e.message}")
            }
        }
    }
}

data class WeatherData(
    val currentTemperature: Double,
    val minTemperature: Double,
    val maxTemperature: Double,
    val apparentTemperature: Double
)

sealed class WeatherState {
    object Loading : WeatherState()
    data class Success(val data: WeatherData) : WeatherState()
    data class Error(val message: String) : WeatherState()
}