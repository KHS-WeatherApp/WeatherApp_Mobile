package com.example.kh_studyprojects_weatherapp.data.api.weather

/**
 * 날씨 API 관련 상수 정의
 * - 쿼리 파라미터 문자열을 중앙에서 관리
 */
object WeatherApiConstants {

    /**
     * 전체 날씨 데이터 쿼리
     * - 현재 날씨: 온도, 습도, 체감온도, 강수량, 날씨 코드, 풍속
     * - 시간별 예보: 온도, 체감온도, 강수 확률, 강수량, 날씨 코드
     * - 일별 예보: 최고/최저 온도, 일출/일몰, UV 지수, 강수량, 풍속
     * - 과거 1일 + 미래 15일 데이터 포함
     */
    const val FULL_WEATHER_QUERY =
        "current=temperature_2m,relative_humidity_2m," +
        "apparent_temperature,is_day,precipitation,weather_code,wind_speed_10m&" +
        "hourly=temperature_2m,apparent_temperature,precipitation_probability," +
        "precipitation,weather_code&" +
        "daily=weather_code,temperature_2m_max,temperature_2m_min," +
        "apparent_temperature_max,apparent_temperature_min,sunrise,sunset," +
        "uv_index_max,precipitation_sum,precipitation_probability_max," +
        "wind_speed_10m_max&" +
        "timezone=auto&past_days=1&forecast_days=15"

    /**
     * 대기질 데이터 쿼리
     * - PM10, PM2.5, UV 지수 (실제/맑은 날 기준)
     */
    const val AIR_QUALITY_QUERY = "current=pm10,pm2_5,uv_index,uv_index_clear_sky"
}