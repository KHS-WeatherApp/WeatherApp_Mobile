package com.example.kh_studyprojects_weatherapp.data.api.kakao

import com.google.gson.annotations.SerializedName

/**
 * 카카오 로컬 API 응답 데이터 모델
 * 
 * 카카오 로컬 API의 coord2regioncode 엔드포인트에서 반환하는 응답 데이터를 담는 클래스입니다.
 * 위도/경도 좌표에 해당하는 주소 정보를 포함합니다.
 *
 * @author 김효동
 * @since 2025.08.06
 * @version 1.0
 */
data class KakaoAddressResponse(
    /** API 응답 메타 정보 */
    @SerializedName("meta")
    val meta: Meta,
    
    /** 주소 정보 목록 (일반적으로 1개 항목) */
    @SerializedName("documents")
    val documents: List<Document>
)

/**
 * 카카오 API 응답 메타 정보
 * 
 * API 응답에 대한 메타데이터를 포함합니다.
 */
data class Meta(
    /** 총 검색 결과 수 */
    @SerializedName("total_count")
    val totalCount: Int
)

/**
 * 카카오 API 주소 정보 문서
 * 
 * 위도/경도 좌표에 해당하는 상세한 주소 정보를 포함합니다.
 * 한국의 행정구역 체계에 따라 1~4depth까지의 주소 정보를 제공합니다.
 */
data class Document(
    /** 지역 타입 (H: 행정구역, B: 지번주소) */
    @SerializedName("region_type")
    val regionType: String,
    
    /** 행정구역 코드 */
    @SerializedName("code")
    val code: String,
    
    /** 전체 주소명 */
    @SerializedName("address_name")
    val addressName: String,
    
    /** 1depth 주소 (시/도) - 예: 서울특별시 */
    @SerializedName("region_1depth_name")
    val region1depthName: String,
    
    /** 2depth 주소 (구/군) - 예: 강남구 */
    @SerializedName("region_2depth_name")
    val region2depthName: String,
    
    /** 3depth 주소 (동/읍/면) - 예: 삼성동 */
    @SerializedName("region_3depth_name")
    val region3depthName: String,
    
    /** 4depth 주소 (리) - 예: 삼성1동 */
    @SerializedName("region_4depth_name")
    val region4depthName: String,
    
    /** 경도 (x좌표) */
    @SerializedName("x")
    val x: String,
    
    /** 위도 (y좌표) */
    @SerializedName("y")
    val y: String
) 