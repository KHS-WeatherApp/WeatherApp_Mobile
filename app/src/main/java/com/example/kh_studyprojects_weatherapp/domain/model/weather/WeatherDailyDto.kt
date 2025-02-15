package com.example.kh_studyprojects_weatherapp.domain.model.weather

data class WeatherDailyDto(
    val type: Type,
    val week: String,
    val date: String,
    val precipitation: String,
    val humidity: String,
    val minTemp: String,
    val maxTemp: String,
    val weatherCode: Int,
    var isVisible: Boolean = true
) {
    enum class Type {
        TODAY, YESTERDAY, OTHER
    }
}