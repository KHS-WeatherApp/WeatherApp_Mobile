package com.example.kh_studyprojects_weatherapp.data.repository.weather

import com.example.kh_studyprojects_weatherapp.domain.repository.weather.WeatherRepository
import com.example.kh_studyprojects_weatherapp.data.api.weather.WeatherRequest
import com.example.kh_studyprojects_weatherapp.data.api.weather.WeatherApiService
import com.example.kh_studyprojects_weatherapp.data.api.weather.WeatherApiConstants
import com.example.kh_studyprojects_weatherapp.domain.model.weather.WeatherCurrent
import com.example.kh_studyprojects_weatherapp.domain.model.weather.WeatherAdditional
import com.example.kh_studyprojects_weatherapp.domain.model.weather.WeatherData
import com.example.kh_studyprojects_weatherapp.data.model.weather.WeatherMappers
import com.example.kh_studyprojects_weatherapp.data.repository.base.BaseRepository
import javax.inject.Inject

/**
 * WeatherRepository 구현체
 * - BaseRepository를 상속하여 공통 에러 처리 로직 활용
 * - Hilt를 통해 WeatherApiService를 주입받고 필요한 의존성을 구성합니다
 */
class WeatherRepositoryImpl @Inject constructor(
    private val weatherApiService: WeatherApiService
) : BaseRepository(), WeatherRepository {
    override val TAG = "WeatherRepository"

    private fun createFullWeatherRequest(latitude: Double, longitude: Double) = WeatherRequest(
        latitude = latitude,
        longitude = longitude,
        queryParam = WeatherApiConstants.FULL_WEATHER_QUERY
    )

    override suspend fun getWeatherInfo(
        latitude: Double,
        longitude: Double
    ): Result<WeatherData> {
        val request = createFullWeatherRequest(latitude, longitude)

        return safeApiCallWithTransform(
            apiCall = { weatherApiService.getWeatherInfo(request) },
            transform = { response -> WeatherMappers.toWeatherData(response) }
        )
    }
    
    // 25.4.27 이선영: '대기질' API 호출 추가
    override suspend fun getAdditionalWeatherInfo(
        latitude: Double,
        longitude: Double
    ): Result<WeatherAdditional> {
        // 1. 기본 날씨 정보 조회
        val weatherData = getWeatherInfo(latitude, longitude)
            .getOrElse { return Result.failure(it) }

        // 2. 대기질 정보 조회
        val requestAir = WeatherRequest(
            latitude = latitude,
            longitude = longitude,
            queryParam = WeatherApiConstants.AIR_QUALITY_QUERY
        )

        return safeApiCallWithTransform(
            apiCall = { weatherApiService.getAdditionalWeatherInfo(requestAir) },
            transform = { airResponse ->
                WeatherMappers.toAdditionalWeather(weatherData, airResponse)
            }
        )
    }

    override suspend fun getCurrentWeather(
        latitude: Double,
        longitude: Double
    ): Result<WeatherCurrent> {
        return getWeatherInfo(latitude, longitude).mapCatching { weatherData ->
            WeatherMappers.toCurrentWeather(weatherData)
                ?: throw IllegalStateException("현재 날씨 정보를 변환할 수 없습니다.")
        }
    }
}