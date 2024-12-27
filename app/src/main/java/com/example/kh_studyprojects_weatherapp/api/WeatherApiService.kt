package com.example.kh_studyprojects_weatherapp.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface WeatherApiService {
    @POST("/api/weather")
    fun getWeatherData(
        @Body params: Map<String, @JvmSuppressWildcards Any>
    ): Call<Map<String, Any>>
}
