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
}
