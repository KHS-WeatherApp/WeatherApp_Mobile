package com.example.kh_studyprojects_weatherapp.data.repository.weather

import android.util.Log
import com.example.kh_studyprojects_weatherapp.domain.repository.weather.WeatherRepository
import com.example.kh_studyprojects_weatherapp.data.api.weather.WeatherRequest
import com.example.kh_studyprojects_weatherapp.data.api.weather.WeatherApiService
import javax.inject.Inject

/**
 * WeatherRepository 구현체
 * - Hilt로 주입 받은 WeatherApiService를 사용하며, 수동 싱글턴 없이 DI로 수명 관리합니다.
 */
class WeatherRepositoryImpl @Inject constructor(
    private val weatherApiService: WeatherApiService
) : WeatherRepository {
    private val TAG = "WeatherRepository"

    override suspend fun getWeatherInfo(
        latitude: Double,
        longitude: Double
    ): Result<Map<String, Any>> {
        return try {
            Log.d(TAG, "API 호출 시작: lat=$latitude, lon=$longitude")
            
            val request = WeatherRequest(
                latitude = latitude,
                longitude = longitude,
                queryParam = "current=temperature_2m,relative_humidity_2m," +
                    "apparent_temperature,is_day,precipitation,weather_code,wind_speed_10m&" +
                    "hourly=temperature_2m,apparent_temperature,precipitation_probability," +
                    "precipitation,weather_code&" +
                    "daily=weather_code,temperature_2m_max,temperature_2m_min," +
                    "apparent_temperature_max,apparent_temperature_min,sunrise,sunset," +
                    "uv_index_max,precipitation_sum,precipitation_probability_max," +
                    "wind_speed_10m_max&" +
                    "timezone=auto&past_days=1&forecast_days=15"
            )

            Log.d(TAG, "요청 데이터: $request")
            val response = weatherApiService.getWeatherInfo(request)
            Log.d(TAG, "응답 코드: ${response.code()}")
            
            if (response.isSuccessful && response.body() != null) {
                Log.d(TAG, "API 호출 성공: ${response.body()}")
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Log.e(TAG, "API 호출 실패: $errorBody")
                Result.failure(Exception("서버 오류: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "API 호출 예외 발생", e)
            
            // 구체적인 에러 메시지 제공
            val errorMessage = when (e) {
                is java.net.SocketTimeoutException -> "서버 연결 시간 초과. 네트워크 상태를 확인해주세요."
                is java.net.ConnectException -> "서버에 연결할 수 없습니다. 백엔드 서버가 실행 중인지 확인해주세요."
                is java.net.UnknownHostException -> "서버 주소를 찾을 수 없습니다. IP 주소를 확인해주세요."
                else -> "날씨 정보를 가져오는데 실패했습니다: ${e.message}"
            }
            
            Result.failure(Exception(errorMessage))
        }
    }
    
    //25.4.27 이수연 : '대기질' 데이터 호출 추가
    override suspend fun getAdditionalWeatherInfo(
        latitude: Double,
        longitude: Double
    ): Result<Map<String, Any>> {
        return try {
            val requestAir = WeatherRequest(
                latitude = latitude,
                longitude = longitude,
                queryParam = "current=pm10,pm2_5,uv_index,uv_index_clear_sky"
            )
            Log.d(TAG, "요청 데이터(대기질): $requestAir")
            val responseAir = weatherApiService.getAdditionalWeatherInfo(requestAir)
            Log.d(TAG, "응답 코드: ${responseAir.code()}")
            Log.d(TAG, "요청 데이터(대기질): ${responseAir}")
            if (responseAir.isSuccessful && responseAir.body() != null) {
                // 데이터의 key를 구분하여 저장
                val airData = responseAir.body()!!.mapKeys { (key, _) ->
                    when (key) {
                        "current" -> "air_current"
                        "daily" -> "air_daily"
                        else -> key
                    }
                }
                Result.success(airData)

            } else {
                val errorBody = responseAir.errorBody()?.string() ?: "Unknown error"
                Log.e(TAG, "대기질 API 호출 실패: $errorBody")
                Result.failure(Exception("대기질 정보 조회 실패: ${responseAir.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "대기질 API 호출 예외 발생", e)
            Result.failure(e)
        }
    }
}

