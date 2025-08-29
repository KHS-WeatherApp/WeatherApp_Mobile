package com.example.kh_studyprojects_weatherapp.data.api.sidemenu

/**
 * 사이드메뉴 즐겨찾기 지역 요청 데이터 모델
 * 
 * 즐겨찾기 지역 추가/수정 시 서버로 전송하는 데이터 구조입니다.
 *
 * @author 김효동
 * @since 2025.08.26
 * @version 1.0
 */
data class SmFavoriteLocationRequest(
    val addressName: String,         // 전체 주소명
    val latitude: Double,            // 위도
    val longitude: Double,           // 경도
    val region1DepthName: String,    // 시/도 (1depth 주소)
    val region2DepthName: String,    // 구/군 (2depth 주소)
    val region3DepthName: String,    // 동/읍/면 (3depth 주소)
    val region3DepthHName: String,   // 동/읍/면 한글명 (3depth 한글 주소)
    val deviceId: String,            // 디바이스 고유 식별자
    val sortOrder: Int = 0           // 정렬 순서 (기본값: 0)
)

