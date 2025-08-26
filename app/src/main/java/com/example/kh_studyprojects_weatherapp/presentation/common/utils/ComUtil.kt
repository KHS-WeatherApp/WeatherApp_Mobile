package com.example.kh_studyprojects_weatherapp.presentation.common.utils

/**
 * 공통 유틸리티 함수들을 모아놓은 클래스
 * 
 * @author 김효동
 * @since 2025.07.20
 * @version 1.0
 */
object ComUtil {
    
    /**
     * 24시간 형식을 12시간 형식으로 변환
     * 
     * @param hour 시간 문자열 (예: "15시")
     * @return 12시간 형식 시간 (예: "3시")
     */
    fun convertTo12HourFormat(hour: String?): String {
        val hourInt = hour?.replace("시", "")?.toIntOrNull() ?: return ""
        return when (hourInt) {
            0 -> "12시"
            in 1..12 -> "${hourInt}시"
            else -> "${hourInt - 12}시"
        }
    }
} 