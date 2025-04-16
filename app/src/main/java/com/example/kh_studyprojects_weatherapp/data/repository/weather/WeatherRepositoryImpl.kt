package com.example.kh_studyprojects_weatherapp.data.repository.weather

import android.util.Log
import com.example.kh_studyprojects_weatherapp.data.api.RetrofitInstance
import com.example.kh_studyprojects_weatherapp.domain.repository.weather.WeatherRepository
import com.example.kh_studyprojects_weatherapp.data.api.weather.WeatherRequest
import javax.inject.Inject

class WeatherRepositoryImpl @Inject constructor(
    // 필요한 의존성들
) : WeatherRepository {
    private val weatherApiService = RetrofitInstance.weatherApiService

    override suspend fun getWeatherInfo(
        latitude: Double,
        longitude: Double
    ): Result<Map<String, Any>> {
        return try {
            Log.d("WeatherRepository", "API 호출 시작: lat=$latitude, lon=$longitude")
            
            val request = WeatherRequest(
                latitude = latitude,
                longitude = longitude,
                queryParam = "current=temperature_2m,relative_humidity_2m," +
                    "apparent_temperature,is_day,precipitation,weather_code&" +
                    "hourly=temperature_2m,apparent_temperature,precipitation_probability," +
                    "precipitation,weather_code&" +
                    "daily=weather_code,temperature_2m_max,temperature_2m_min," +
                    "apparent_temperature_max,apparent_temperature_min,sunrise,sunset," +
                    "uv_index_max,precipitation_sum,precipitation_probability_max," +
                    "wind_speed_10m_max&" +
                    "timezone=auto&past_days=1&forecast_days=15"
            )

            Log.d("WeatherRepository", "요청 데이터: $request")
            val response = weatherApiService.getWeatherInfo(request)
            Log.d("WeatherRepository", "응답 코드: ${response.code()}")
            
            if (response.isSuccessful && response.body() != null) {
                Log.d("WeatherRepository", "API 호출 성공: ${response.body()}")
                Result.success(response.body()!!)
            } else {
                Log.e("WeatherRepository", "API 호출 실패: ${response.errorBody()?.string()}")
                Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("WeatherRepository", "API 호출 예외 발생", e)
            Result.failure(e)
        }
    }

    companion object {
        // Singleton 인스턴스 생성
        @Volatile
        private var instance: WeatherRepositoryImpl? = null

        fun getInstance(): WeatherRepositoryImpl {
            return instance ?: synchronized(this) {
                instance ?: WeatherRepositoryImpl().also { instance = it }
            }
        }
    }
}