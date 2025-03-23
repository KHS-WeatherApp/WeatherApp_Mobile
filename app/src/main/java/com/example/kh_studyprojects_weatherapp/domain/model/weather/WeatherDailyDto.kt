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
    var isVisible: Boolean = true,
    // 기존 필드들...
    val globalMinTemp: Double, // 전체 기간 최저 온도
    val globalMaxTemp: Double  // 전체 기간 최고 온도
) {
    enum class Type {
        TODAY, YESTERDAY, OTHER
    }
}