package com.example.kh_studyprojects_weatherapp.data.api.weather

data class WeatherResponse(
    val current: Current,
    val daily: Daily
)

data class Current(
    val time: String,
    val temperature_2m: Double,
    val relative_humidity_2m: Int,
    val apparent_temperature: Double,
    val is_day: Int,
    val precipitation: Double,
    val weather_code: Int
)

data class Daily(
    val temperature_2m_min: List<Double>,
    val temperature_2m_max: List<Double>
)