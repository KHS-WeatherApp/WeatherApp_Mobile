package com.example.kh_studyprojects_weatherapp.domain.model.weather

data class WeatherCurrent(
    val location: String,
    val temperature: Int?,
    val apparentTemperature: Int?,
    val weatherCode: Int,
    val currentTimeIso: String?,
    val hourlyTimes: List<String> = emptyList(),
    val hourlyTemperatures: List<Double> = emptyList(),
    val todayMaxTemp: Double? = null,
    val todayMinTemp: Double? = null
)
