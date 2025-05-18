package com.example.kh_studyprojects_weatherapp.domain.repository.weather

interface WeatherRepository {
    suspend fun getWeatherInfo(
        latitude: Double, 
        longitude: Double
    ): Result<Map<String, Any>>

    //25.4.27 이수연 : '대기질' 데이터 호출 추가
    suspend fun getAdditionalWeatherInfo(
        latitude: Double,
        longitude: Double
    ): Result<Map<String, Any>>
}