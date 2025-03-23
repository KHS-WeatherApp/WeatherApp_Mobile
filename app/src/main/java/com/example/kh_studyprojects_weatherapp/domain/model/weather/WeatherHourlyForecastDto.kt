package com.example.kh_studyprojects_weatherapp.domain.model.weather

class WeatherHourlyForecastDto (
    val tvAmPm: String?,            // AM/PM
    val tvHour: String?,            // 시간
    val probability: String?,       // 강수 확률
    val precipitation: String?,     // 강수량
    val temperature: String?        // 온도
)
