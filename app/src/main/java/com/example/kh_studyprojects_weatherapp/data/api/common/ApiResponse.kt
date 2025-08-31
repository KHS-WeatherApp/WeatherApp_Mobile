package com.example.kh_studyprojects_weatherapp.data.api.common

/**
 * API 응답 표준화 래퍼 클래스
 * 
 * 서버에서 보내는 표준화된 응답 형식을 처리하기 위한 데이터 클래스
 * 
 * @param T 응답 데이터의 타입
 */
data class ApiResponse<T>(
    val success: Boolean,           // API 요청 처리 성공/실패 여부
    val message: String,            // 응답 메시지
    val data: T?,                   // 응답 데이터
    val timestamp: Long             // 응답 생성 시간 (Unix timestamp)
)
