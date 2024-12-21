package com.example.kh_studyprojects_weatherapp.weather.api
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

// weather/api/WeatherApiService.kt
interface WeatherApiService {
    @POST("/api/weather")
    suspend fun getWeatherInfo(
        @Body request: WeatherRequest
    ): Response<Map<String, Any>> // 서버 응답 형식에 맞게 수정 가능
}