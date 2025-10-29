package com.example.kh_studyprojects_weatherapp.domain.model.weather

import com.example.kh_studyprojects_weatherapp.data.model.weather.WeatherHourlyForecastDto

/**
 * 시간별 예보 화면의 UiState 데이터 모델
 * - BaseLoadViewModel<T>의 제네릭 타입으로 사용됩니다
 *
 * @property hourlyForecastItems 시간별 예보 리스트
 * @property locationInfo 위치 정보 (주소 + 위경도)
 * @property currentApiTime API가 알려준 현재 시각(ISO 문자열, 예: 2025-10-29T23:00)
 */
data class WeatherHourlyForecastUiData(
    val hourlyForecastItems: List<WeatherHourlyForecastDto>,
    val locationInfo: String,
    val currentApiTime: String
)
