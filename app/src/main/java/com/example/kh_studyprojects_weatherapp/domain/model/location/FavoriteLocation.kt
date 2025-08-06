package com.example.kh_studyprojects_weatherapp.domain.model.location

/**
 * 즐겨찾기 위치 정보 데이터 클래스
 * 
 * 사용자가 즐겨찾기로 등록한 위치 정보를 담는 도메인 모델입니다.
 * 현재 위치와 사용자가 직접 추가한 위치를 구분하여 관리합니다.
 * 카카오 로컬 API로 변환된 한국어 주소 정보를 포함합니다.
 *
 * @author 김효동
 * @since 2025.08.06
 * @version 1.0
 */
data class FavoriteLocation(
    /** 위치 고유 식별자 */
    val id: String,
    
    /** 위치 이름 (사용자가 지정한 이름 또는 주소) */
    val name: String,
    
    /** 위도 좌표 */
    val latitude: Double,
    
    /** 경도 좌표 */
    val longitude: Double,
    
    /** 카카오 API로 변환된 한국어 주소 */
    val address: String,
    
    /** 현재 위치 여부 (true: 현재 위치, false: 사용자가 추가한 위치) */
    val isCurrentLocation: Boolean = false
) 