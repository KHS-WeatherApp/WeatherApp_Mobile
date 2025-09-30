package com.example.kh_studyprojects_weatherapp.domain.model.weather

data class WeatherAdditional(
    val sunrise: String?,
    val sunset: String?,
    val precipitation: Double?,
    val windSpeed: Double?,
    val pm10: Double?,
    val pm2_5: Double?,
    val uvIndex: Double?
)

