package com.example.kh_studyprojects_weatherapp.domain.repository.weather

import com.example.kh_studyprojects_weatherapp.domain.model.weather.WeatherAdditional
import com.example.kh_studyprojects_weatherapp.domain.model.weather.WeatherCurrent
import com.example.kh_studyprojects_weatherapp.domain.model.weather.WeatherData

interface WeatherRepository {
    suspend fun getWeatherInfo(
        latitude: Double,
        longitude: Double
    ): Result<WeatherData>

    suspend fun getAdditionalWeatherInfo(
        latitude: Double,
        longitude: Double
    ): Result<WeatherAdditional>

    suspend fun getCurrentWeather(
        latitude: Double,
        longitude: Double
    ): Result<WeatherCurrent>
}
