package com.example.kh_studyprojects_weatherapp.domain.model.weather

import com.example.kh_studyprojects_weatherapp.data.model.weather.WeatherDailyDto

/**
 * 일별 예보 화면의 UiState 데이터 모델
 * - BaseLoadViewModel<T>의 제네릭 타입으로 사용됩니다
 *
 * @property weatherItems 일별 예보 리스트
 * @property currentApiTime 현재 API 시간
 * @property locationInfo 위치 정보 (주소 + 위경도)
 */
data class WeatherDailyUiData(
    val weatherItems: List<WeatherDailyDto>,
    val currentApiTime: String,
    val locationInfo: String
)
