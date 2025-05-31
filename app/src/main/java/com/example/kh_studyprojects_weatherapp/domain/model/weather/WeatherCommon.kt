package com.example.kh_studyprojects_weatherapp.domain.model.weather

import com.example.kh_studyprojects_weatherapp.R

/**
 * 날씨 관련 공통 매핑 규칙을 정의하는 도메인 모델
 * 
 * @author 김효동
 * @since 2025.05.18
 * @version 1.0
 */
object WeatherCommon {


    /**
     * 날씨 코드에 따른 아이콘 리소스 ID를 반환
     * 
     * @param weatherCode 날씨 코드
     * @return 아이콘 리소스 ID
     */
    fun getWeatherIcon(weatherCode: Int): Int = when (weatherCode) {
        0 -> R.drawable.weather_icon_sun
        1, 2, 3 -> R.drawable.weather_icon_partly_cloudy
        45, 48 -> R.drawable.weather_icon_fog
        51, 53, 55 -> R.drawable.weather_icon_drizzle
        56, 57 -> R.drawable.weather_icon_freezing_drizzle
        61, 63, 65 -> R.drawable.weather_icon_shower
        66, 67 -> R.drawable.weather_icon_shower
        71, 73, 75 -> R.drawable.weather_icon_snow
        77 -> R.drawable.weather_icon_snow
        80, 81, 82 -> R.drawable.weather_icon_thunder
        85, 86 -> R.drawable.weather_icon_thunder
        95 -> R.drawable.weather_icon_thunder
        96, 99 -> R.drawable.weather_icon_thunder
        else -> R.drawable.weather_icon_unknown
    }

    /**
     * 온도에 따른 옷 아이콘 리소스 ID를 반환
     * 
     * @param temperature 온도
     * @return 옷 아이콘 리소스 ID
     */
    fun getClothingIcon(temperature: Double): Int = when {
        temperature >= 28 -> R.drawable.clothing_icon_hawaiianshirt
        temperature >= 23 -> R.drawable.clothing_icon_hawaiianshirt
        temperature >= 20 -> R.drawable.clothing_icon_hawaiianshirt
        temperature >= 17 -> R.drawable.clothing_icon_hawaiianshirt
        temperature >= 12 -> R.drawable.clothing_icon_hawaiianshirt
        temperature >= 9 -> R.drawable.clothing_icon_hawaiianshirt
        else -> R.drawable.clothing_icon_hawaiianshirt
    }

    /**
     * 온도에 따른 배경 리소스 ID를 반환
     * 
     * @param temperature 온도
     * @return 배경 리소스 ID
     */
    fun getBackgroundForTemperature(temperature: Double): Int = when {
        temperature >= 30.0 -> R.drawable.sh_hourly_round_temperature_30
        temperature >= 25.0 -> R.drawable.sh_hourly_round_temperature_20
        temperature >= 20.0 -> R.drawable.sh_hourly_round_temperature_20
        temperature >= 15.0 -> R.drawable.sh_hourly_round_temperature_15
        temperature >= 10.0 -> R.drawable.sh_hourly_round_temperature_10
        else -> R.drawable.sh_hourly_round_temperature_10
    }
}