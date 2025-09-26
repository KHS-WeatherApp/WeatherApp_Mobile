package com.example.kh_studyprojects_weatherapp.data.model.weather

import android.util.Log
import java.time.LocalDateTime

/**
 * WeatherMappers
 * - 서버 응답(Map 형태)을 안전하게 파싱해 화면에서 사용하는 DTO로 변환합니다.
 * - 핵심 목표는 프레젠테이션 계층에서의 무분별한 캐스팅과 인덱스 예외를 줄이고,
 *   파싱 로직을 한 곳(데이터 계층)으로 모아 재사용성과 테스트 용이성을 높이는 것입니다.
 *
 * 기대 입력(response)의 예시(간략):
 *   {
 *     "hourly": {
 *       "time": ["2025-09-26T10:00Z", ...],
 *       "temperature_2m": [23.1, ...],
 *       "precipitation_probability": [0, 10, ...],
 *       "precipitation": [0.0, 0.2, ...],
 *       "weather_code": [3, ...],
 *       "apparent_temperature": [24.0, ...]
 *     },
 *     ...
 *   }
 *
 * 주의 사항:
 * - 키가 없거나 타입이 다르면 안전하게 빈 리스트를 반환합니다(앱 크래시 방지).
 * - 시간 문자열은 ISO-8601 형태를 가정하며 뒤의 "Z"를 제거해 LocalDateTime으로 파싱합니다.
 * - 현재 시각(시 단위 정규화) 이후의 항목부터 최대 24개만 반환합니다.
 */
object WeatherMappers {
    // ---------- Helpers ----------
    private fun asStringList(list: List<*>?): List<String> =
        list?.map { it?.toString() ?: "" } ?: emptyList()

    private fun asDoubleList(list: List<*>?, fallbackSize: Int = 0, fallback: Double = 0.0): List<Double> =
        list?.map {
            when (it) {
                is Number -> it.toDouble()
                is String -> it.toDoubleOrNull() ?: fallback
                else -> fallback
            }
        } ?: List(fallbackSize) { fallback }

    private fun asIntList(list: List<*>?, fallbackSize: Int = 0, fallback: Int = 0): List<Int> =
        list?.map {
            when (it) {
                is Number -> it.toInt()
                is String -> it.toIntOrNull() ?: fallback
                else -> fallback
            }
        } ?: List(fallbackSize) { fallback }

    private fun parseToLocalDateTime(raw: String): LocalDateTime? {
        // Try common patterns: with 'Z', with offset, without seconds, with seconds
        val s = raw.trim()
        return try {
            // 1) Remove trailing 'Z' if present and parse
            val noZ = if (s.endsWith("Z", ignoreCase = true)) s.dropLast(1) else s
            LocalDateTime.parse(noZ)
        } catch (_: Exception) {
            try {
                java.time.OffsetDateTime.parse(s).toLocalDateTime()
            } catch (_: Exception) {
                try {
                    // Last resort: if it contains seconds-less, append :00
                    val patched = if (s.matches(Regex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}Z"))) s.replace("Z", ":00") else s
                    val noZ = patched.replace("Z", "")
                    LocalDateTime.parse(noZ)
                } catch (_: Exception) {
                    null
                }
            }
        }
    }

    /**
     * 전체 응답에서 hourly 섹션을 파싱해 시간별 예보 DTO 리스트로 변환합니다.
     *
     * 변환 규칙:
     * - AM/PM 표기: 0~11시는 "오전", 그 외 "오후"
     * - 시(hour) 표기: 0,12시는 "12", 나머지는 (hour % 12)
     * - 강수확률/강수량: 0 이하면 공백 처리(불필요한 정보 노출 방지)
     * - 리스트 길이 불일치나 인덱스 초과는 getOrNull로 방어
     *
     * @param response 서버 응답 맵(최상위)
     * @return 24개 이내의 시간별 예보 DTO 목록(없으면 빈 리스트)
     */
    fun toHourlyForecastDtos(response: Map<String, Any>): List<WeatherHourlyForecastDto> {
        // 1) hourly 블록 추출 (없으면 빈 리스트)
        val hourlyData = response["hourly"] as? Map<*, *> ?: return emptyList()

        // 2) 필요한 키별 리스트 추출(타입 유연 처리)
        val timesRaw = hourlyData["time"] as? List<*> ?: return emptyList()
        val times = asStringList(timesRaw)

        val temperatures = asDoubleList(hourlyData["temperature_2m"] as? List<*>, fallbackSize = times.size)
        val precipitationProbs = asIntList(hourlyData["precipitation_probability"] as? List<*>, fallbackSize = times.size)
        val precipitations = asDoubleList(hourlyData["precipitation"] as? List<*>, fallbackSize = times.size)
        val weatherCodes = asIntList(hourlyData["weather_code"] as? List<*>, fallbackSize = times.size)
        val apparentTemps = asDoubleList(hourlyData["apparent_temperature"] as? List<*>, fallbackSize = times.size)

        // 3) 현재 시각(분/초/나노 0 처리) 이후의 첫 인덱스 계산
        val now = LocalDateTime.now()
        val currentHourStart = now.withMinute(0).withSecond(0).withNano(0)

        val currentIndex = times.indexOfFirst { time ->
            val dateTime = parseToLocalDateTime(time)
            dateTime != null && !dateTime.isBefore(currentHourStart)
        }.takeIf { it >= 0 } ?: 0

        // 4) 현재 시각 기준 최대 24개만 수집
        val result = mutableListOf<WeatherHourlyForecastDto>()
        var hoursCount = 0

        while (hoursCount < 24 && (currentIndex + hoursCount) < times.size) {
            try {
                val index = currentIndex + hoursCount
                val time = times[index]
                val dateTime = parseToLocalDateTime(time) ?: continue
                val hourInt = dateTime.hour

                val amPm = if (hourInt in 0..11) "오전" else "오후"
                val formattedHour = if (hourInt == 0 || hourInt == 12) "12" else (hourInt % 12).toString()
                val probVal = precipitationProbs.getOrNull(index) ?: 0
                val precipVal = precipitations.getOrNull(index) ?: 0.0
                val tempVal = temperatures.getOrNull(index) ?: 0.0
                val codeVal = weatherCodes.getOrNull(index) ?: 0
                val appTempVal = apparentTemps.getOrNull(index) ?: 0.0

                val prob = if (probVal > 0) "$probVal%" else ""
                val precip = if (precipVal > 0) "${precipVal}mm" else ""
                val temp = "$tempVal"
                val weatherCode = codeVal
                val apparentTemp = "$appTempVal"

                result.add(
                    WeatherHourlyForecastDto(
                        tvAmPm = amPm,
                        tvHour = formattedHour,
                        probability = prob,
                        precipitation = precip,
                        temperature = temp,
                        weatherCode = weatherCode,
                        apparent_temperature = apparentTemp
                    )
                )
                hoursCount++
            } catch (e: Exception) {
                // 개별 항목 파싱 실패 시 루프 중단(불완전 데이터 확산 방지)
                Log.e("WeatherMappers", "Hourly forecast parse error", e)
                break
            }
        }

        return result
    }

    /**
     * Current 화면용: 응답 맵에 안전하게 위치 문자열을 합성합니다.
     * 프레젠테이션 계층이 임의 캐스팅 없이 그대로 사용할 수 있도록 도와줍니다.
     */
    fun enrichCurrentWeather(response: Map<String, Any>, locationAddress: String): Map<String, Any> {
        return try {
            val copy = response.toMutableMap()
            copy["location"] = locationAddress
            copy
        } catch (_: Exception) {
            mapOf("location" to locationAddress)
        }
    }

    /**
     * Additional 화면용: 날씨/대기질 응답을 하나의 맵으로 병합합니다.
     * 동일 키 충돌 시 airData가 우선(최근 호출 기준)하도록 합칩니다.
     */
    fun mergeWeatherAndAir(weatherData: Map<String, Any>?, airData: Map<String, Any>?): Map<String, Any> {
        val result = mutableMapOf<String, Any>()
        if (weatherData != null) result.putAll(weatherData)
        if (airData != null) result.putAll(airData)
        return result
    }

    /**
     * Daily 화면용: 일별/시간별 데이터를 결합해 WeatherDailyDto 목록으로 변환합니다.
     * - 일자별 최소/최대, 강수량, 강수확률(또는 유사 지표), 일자별 시간예보 리스트를 포함합니다.
     * - 날짜 표기는 index 기준으로 0: 어제, 1: 오늘, 나머지: 요일명과 M.d 형식 병행.
     */
    fun toDailyWeatherDtos(response: Map<String, Any>): List<WeatherDailyDto> {
        val daily = response["daily"] as? Map<*, *> ?: return emptyList()
        val hourly = response["hourly"] as? Map<*, *> ?: return emptyList()

        val dailyTime = asStringList(daily["time"] as? List<*>)
        if (dailyTime.isEmpty()) return emptyList()

        val maxTemps = asDoubleList(daily["temperature_2m_max"] as? List<*>, fallbackSize = dailyTime.size)
        val minTemps = asDoubleList(daily["temperature_2m_min"] as? List<*>, fallbackSize = dailyTime.size)
        val weatherCodes = asIntList(daily["weather_code"] as? List<*>, fallbackSize = dailyTime.size)
        val precipitations = asDoubleList(daily["precipitation_sum"] as? List<*>, fallbackSize = dailyTime.size)
        val humidities = asIntList(daily["precipitation_probability_max"] as? List<*>, fallbackSize = dailyTime.size)
        // 주의: API 필드 네이밍에 따라 max/min이 반대로 들어올 수 있어 원본 로직을 존중해 매핑
        val apparentTempMaxs = asDoubleList(daily["apparent_temperature_min"] as? List<*>, fallbackSize = dailyTime.size)
        val apparentTempMins = asDoubleList(daily["apparent_temperature_max"] as? List<*>, fallbackSize = dailyTime.size)

        val hourlyTimes = asStringList(hourly["time"] as? List<*>)
        val hourlyTemps = asDoubleList(hourly["temperature_2m"] as? List<*>, fallbackSize = hourlyTimes.size)
        val hourlyPrecip = asDoubleList(hourly["precipitation"] as? List<*>, fallbackSize = hourlyTimes.size)
        val hourlyProb = asIntList(hourly["precipitation_probability"] as? List<*>, fallbackSize = hourlyTimes.size)
        val hourlyCodes = asIntList(hourly["weather_code"] as? List<*>, fallbackSize = hourlyTimes.size)
        val hourlyApparentTemps = asDoubleList(hourly["apparent_temperature"] as? List<*>, fallbackSize = hourlyTimes.size)

        val hourlyPerDay: Map<String, List<String>> = hourlyTimes.groupBy { it.take(10) }
        val lowestTemp = (minTemps.minOrNull() ?: -18.0)
        val highestTemp = (maxTemps.maxOrNull() ?: 38.0)

        return dailyTime.mapIndexed { index, date ->
            val hourlyDataList = hourlyPerDay[date]

            val hourlyForecast: List<WeatherHourlyForecastDto> = hourlyDataList?.mapNotNull { timeStr ->
                try {
                    val idx = hourlyTimes.indexOf(timeStr)
                    val hour = parseToLocalDateTime(timeStr)?.hour ?: return@mapNotNull null
                    WeatherHourlyForecastDto(
                        tvAmPm = if (hour < 12) "오전" else "오후",
                        tvHour = if (hour == 0 || hour == 12) "12" else (hour % 12).toString(),
                        probability = "${hourlyProb.getOrNull(idx) ?: 0}%",
                        precipitation = "${hourlyPrecip.getOrNull(idx) ?: 0.0}mm",
                        temperature = "${hourlyTemps.getOrNull(idx) ?: 0.0}°",
                        weatherCode = hourlyCodes.getOrNull(idx) ?: 0,
                        apparent_temperature = "${hourlyApparentTemps.getOrNull(idx) ?: 0.0}°",
                    )
                } catch (_: Exception) {
                    null
                }
            } ?: emptyList()

            WeatherDailyDto(
                type = when (index) {
                    0 -> WeatherDailyDto.Type.YESTERDAY
                    1 -> WeatherDailyDto.Type.TODAY
                    else -> WeatherDailyDto.Type.OTHER
                },
                week = when (index) {
                    0 -> "어제"
                    1 -> "오늘"
                    else -> dayOfWeekKorean(date)
                },
                date = if (index == 0 || index == 1) date else formatDateKorean(date),
                precipitation = "${precipitations.getOrNull(index) ?: 0.0}mm",
                humidity = "${humidities.getOrNull(index) ?: 0}%",
                minTemp = "${minTemps.getOrNull(index) ?: 0.0}°",
                maxTemp = "${maxTemps.getOrNull(index) ?: 0.0}°",
                weatherCode = weatherCodes.getOrNull(index) ?: 0,
                isVisible = true,
                globalMinTemp = lowestTemp,
                globalMaxTemp = highestTemp,
                hourlyForecast = hourlyForecast,
                apparent_temperature_max = "${apparentTempMaxs.getOrNull(index) ?: 0.0}°",
                apparent_temperature_min = "${apparentTempMins.getOrNull(index) ?: 0.0}°",
            )
        }
    }

    private fun dayOfWeekKorean(dateString: String): String = try {
        val date = java.time.LocalDate.parse(dateString)
        when (date.dayOfWeek) {
            java.time.DayOfWeek.MONDAY -> "월"
            java.time.DayOfWeek.TUESDAY -> "화"
            java.time.DayOfWeek.WEDNESDAY -> "수"
            java.time.DayOfWeek.THURSDAY -> "목"
            java.time.DayOfWeek.FRIDAY -> "금"
            java.time.DayOfWeek.SATURDAY -> "토"
            java.time.DayOfWeek.SUNDAY -> "일"
        }
    } catch (_: Exception) { "?" }

    private fun formatDateKorean(dateString: String): String = try {
        val d = java.time.LocalDate.parse(dateString)
        "${d.monthValue}.${d.dayOfMonth}"
    } catch (_: Exception) { dateString }
}
