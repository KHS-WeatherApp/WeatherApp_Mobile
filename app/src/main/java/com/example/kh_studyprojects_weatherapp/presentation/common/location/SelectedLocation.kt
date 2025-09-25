package com.example.kh_studyprojects_weatherapp.presentation.common.location

/**
 * 사용자가 선택한 위치 값을 나타냅니다.
 *
 * @property latitude 위도
 * @property longitude 경도
 * @property address 주소 문자열(없을 수 있음)
 */
data class SelectedLocation(
    val latitude: Double,
    val longitude: Double,
    val address: String?
)


