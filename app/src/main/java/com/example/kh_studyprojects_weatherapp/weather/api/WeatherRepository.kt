package com.example.kh_studyprojects_weatherapp.weather.api

// weather/api/WeatherRepository.kt
class WeatherRepository {
    private val weatherApiService = RetrofitInstance.weatherApiService

    suspend fun getWeatherInfo(latitude: Double, longitude: Double): Result<Map<String, Any>> {
        return try {
            val request = WeatherRequest(
                latitude = latitude,
                longitude = longitude,
                queryParam = "current=temperature_2m,relative_humidity_2m,apparent_temperature,is_day,precipitation,weather_code&hourly=temperature_2m,apparent_temperature,precipitation_probability,precipitation,weather_code&daily=weather_code,temperature_2m_max,temperature_2m_min,apparent_temperature_max,apparent_temperature_min,sunrise,sunset,uv_index_max,precipitation_sum,precipitation_probability_max,wind_speed_10m_max&timezone=auto&past_days=1&forecast_days=14"
            )

            val response = weatherApiService.getWeatherInfo(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}