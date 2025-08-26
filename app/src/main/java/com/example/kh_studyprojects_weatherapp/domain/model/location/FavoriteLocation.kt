package com.example.kh_studyprojects_weatherapp.domain.model.location

/**
 * 즐겨찾기 위치 정보 데이터 클래스
 * 
 * 사용자가 즐겨찾기로 등록한 위치 정보를 담는 도메인 모델입니다.
 * 현재 위치와 사용자가 직접 추가한 위치를 구분하여 관리합니다.
 * 카카오 로컬 API로 변환된 한국어 주소 정보를 포함합니다.
 * DB 테이블 COM_FAVORITE_LOCATIONS와 연동됩니다.
 *
 * @author 김효동
 * @since 2025.08.06
 * @version 1.0
 */
data class FavoriteLocation(
    /** 디바이스 고유 식별자 */
    val deviceId: String,
    
    /** 위도 좌표 (Y좌표) */
    val latitude: Double,
    
    /** 경도 좌표 (X좌표) */
    val longitude: Double,
    
    /** 전체 주소명 (카카오 API로 변환된 한국어 주소) */
    val addressName: String,
    
    /** 시/도 (1depth 주소) */
    val region1depthName: String? = null,
    
    /** 구/군 (2depth 주소) */
    val region2depthName: String? = null,
    
    /** 동/읍/면 (3depth 주소) */
    val region3depthName: String? = null,
    
    /** 사용자 정렬순서 */
    val sortOrder: Int = 0
) 