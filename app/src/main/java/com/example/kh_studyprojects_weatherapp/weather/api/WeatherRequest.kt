package com.example.kh_studyprojects_weatherapp.weather.api

// weather/api/WeatherRequest.kt
data class WeatherRequest(
    val latitude: Double,
    val longitude: Double,
    val queryParam: String
)