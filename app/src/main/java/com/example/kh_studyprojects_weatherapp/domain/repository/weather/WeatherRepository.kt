package com.example.kh_studyprojects_weatherapp.domain.repository.weather

interface WeatherRepository {
    suspend fun getWeatherInfo(
        latitude: Double, 
        longitude: Double
    ): Result<Map<String, Any>>
}