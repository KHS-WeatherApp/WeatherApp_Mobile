package com.example.kh_studyprojects_weatherapp.presentation.common.base

/**
 * 날씨 데이터 새로고침이 가능한 Fragment 인터페이스
 *
 * WeatherFragment의 자식 Fragment들이 구현하여
 * 통일된 방식으로 데이터 새로고침을 제공합니다.
 *
 * @author 김효동
 * @since 2025.10.13
 * @version 1.0
 */
interface RefreshableFragment {
    /**
     * 날씨 데이터를 새로고침합니다.
     */
    fun refreshWeatherData()
}
