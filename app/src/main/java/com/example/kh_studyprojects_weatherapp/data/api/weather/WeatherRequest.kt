package com.example.kh_studyprojects_weatherapp.data.api.weather

// weather/api/WeatherRequest.kt
data class WeatherRequest(
    val latitude: Double,
    val longitude: Double,
    val queryParam: String
)