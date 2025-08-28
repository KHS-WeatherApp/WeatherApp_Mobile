package com.example.kh_studyprojects_weatherapp.data.api.weather

import com.google.gson.annotations.SerializedName

// weather/api/WeatherRequest.kt
data class WeatherRequest(
    @SerializedName("latitude")
    val latitude: Double,
    @SerializedName("longitude")
    val longitude: Double,
    @SerializedName("queryParam")
    val queryParam: String
)