package com.example.kh_studyprojects_weatherapp.data.api.sidemenu

/**
 * 사이드메뉴 즐겨찾기 지역 응답 데이터 모델
 * 
 * 서버에서 즐겨찾기 지역 정보를 응답할 때 사용하는 데이터 구조입니다.
 *
 * @author 김효동
 * @since 2025.08.26
 * @version 1.0
 */
data class SmFavoriteLocationResponse(
    val seqNo: Long,                // 즐겨찾기 지역 고유 번호
    val deviceId: String,           // 디바이스 고유 식별자
    val latitude: Double,           // 위도
    val longitude: Double,          // 경도
    val addressName: String,        // 전체 주소명
    val region1DepthName: String?,  // 시/도 (1depth 주소)
    val region2DepthName: String?,  // 구/군 (2depth 주소)
    val region3DepthName: String?,  // 동/읍/면 (3depth 주소)
    val region3DepthHName: String?, // 동/읍/면 한글명 (3depth 한글 주소)
    val sortOrder: Int,             // 정렬 순서
    val createdAt: String           // 생성일시
)

