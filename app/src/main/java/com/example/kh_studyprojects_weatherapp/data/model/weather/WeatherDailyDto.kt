package com.example.kh_studyprojects_weatherapp.data.model.weather

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

    val globalMinTemp: Double, // 전체 기간 최저 온도
    val globalMaxTemp: Double,  // 전체 기간 최고 온도
    val hourlyForecast: List<WeatherHourlyForecastDto> = emptyList(), // 시간별 날씨
    val apparent_temperature_max: String,      // 체감온도 최고
    val apparent_temperature_min: String
    // 체감온도 최저
) {
    enum class Type {
        TODAY, YESTERDAY, OTHER
    }
}