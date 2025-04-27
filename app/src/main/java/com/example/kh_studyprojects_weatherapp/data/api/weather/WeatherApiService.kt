package com.example.kh_studyprojects_weatherapp.data.api.weather
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

    //25.4.27 이수연 : '대기질' 데이터 호출 추가
    @POST("/api/airPollution")
    suspend fun getAdditionalWeatherInfo(
        @Body request: WeatherRequest
    ): Response<Map<String, Any>>
}