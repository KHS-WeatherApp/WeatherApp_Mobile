
package com.example.kh_studyprojects_weatherapp.data

import com.example.kh_studyprojects_weatherapp.type.ViewType

data class WeatherData(
    val week: String,
    val date: String,
    val precipitation: String,
    val humidity: String,
    val minTemp: String,
    val maxTemp: String,
    val type: ViewType, // ViewType을 구분하기 위한 필드
    val weatherEmoji: String, // 날씨 이모티콘 추가
    var isVisible: Boolean = true // 데이터가 보이는지 여부를 관리하는 필드
)

