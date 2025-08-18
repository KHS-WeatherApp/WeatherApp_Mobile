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

/**
 * 카카오 로컬 API 주소 검색 응답 데이터 모델
 * 
 * 카카오 로컬 API의 keyword 엔드포인트에서 반환하는 응답 데이터를 담는 클래스입니다.
 * 키워드 검색 결과로 반환되는 주소 정보를 포함합니다.
 *
 * @author 김효동
 * @since 2025.08.14
 * @version 1.0
 */
data class KakaoSearchResponse(
    /** API 응답 메타 정보 */
    @SerializedName("meta")
    val meta: SearchMeta,
    
    /** 주소 정보 목록 */
    @SerializedName("documents")
    val documents: List<KakaoDocument>
)

/**
 * 카카오 검색 API 응답 메타 정보
 */
data class SearchMeta(
    /** 총 검색 결과 수 */
    @SerializedName("total_count")
    val totalCount: Int,
    
    /** 현재 페이지의 결과 수 */
    @SerializedName("pageable_count")
    val pageableCount: Int,
    
    /** 마지막 페이지 여부 */
    @SerializedName("is_end")
    val isEnd: Boolean
)

/**
 * 카카오 로컬 API 주소 검색 결과 문서
 * 
 * 키워드 검색을 통해 반환되는 주소 정보를 포함합니다.
 * 지번주소와 도로명주소 정보를 모두 제공합니다.
 *
 * @author 김효동
 * @since 2025.08.14
 * @version 1.0
 */
data class KakaoDocument(
    /** 장소 ID */
    @SerializedName("id")
    val id: String,
    
    /** 장소명 */
    @SerializedName("place_name")
    val placeName: String,
    
    /** 카테고리명 */
    @SerializedName("category_name")
    val categoryName: String,
    
    /** 카테고리 그룹 */
    @SerializedName("category_group_code")
    val categoryGroupCode: String,
    
    /** 카테고리 그룹명 */
    @SerializedName("category_group_name")
    val categoryGroupName: String,
    
    /** 전화번호 */
    @SerializedName("phone")
    val phone: String,
    
    /** 전체 주소명 */
    @SerializedName("address_name")
    val addressName: String,
    
    /** 도로명 주소 */
    @SerializedName("road_address_name")
    val roadAddressName: String,
    
    /** 경도 (x좌표) */
    @SerializedName("x")
    val x: String,
    
    /** 위도 (y좌표) */
    val y: String,
    
    /** 지번주소 상세 정보 */
    @SerializedName("address")
    val address: KakaoAddress?,
    
    /** 도로명주소 상세 정보 */
    @SerializedName("road_address")
    val roadAddress: KakaoRoadAddress?,
    
    /** 장소 URL */
    @SerializedName("place_url")
    val placeUrl: String,
    
    /** 거리 (현재 위치 기준, 미터 단위) */
    @SerializedName("distance")
    val distance: String
)

/**
 * 카카오 지번주소 상세 정보
 */
data class KakaoAddress(
    /** 지번주소명 */
    @SerializedName("address_name")
    val addressName: String,
    
    /** 지역 1depth */
    @SerializedName("region_1depth_name")
    val region1depthName: String,
    
    /** 지역 2depth */
    @SerializedName("region_2depth_name")
    val region2depthName: String,
    
    /** 지역 3depth */
    @SerializedName("region_3depth_name")
    val region3depthName: String,
    
    /** 지역 4depth */
    @SerializedName("region_4depth_name")
    val region4depthName: String,
    
    /** 지번 본번 */
    @SerializedName("main_address_no")
    val mainAddressNo: String,
    
    /** 지번 부번 */
    @SerializedName("sub_address_no")
    val subAddressNo: String,
    
    /** 우편번호 */
    @SerializedName("zip_code")
    val zipCode: String
)

/**
 * 카카오 도로명주소 상세 정보
 */
data class KakaoRoadAddress(
    /** 도로명주소명 */
    @SerializedName("address_name")
    val addressName: String,
    
    /** 도로명 */
    @SerializedName("road_name")
    val roadName: String,
    
    /** 건물 본번 */
    @SerializedName("main_building_no")
    val mainBuildingNo: String,
    
    /** 건물 부번 */
    @SerializedName("sub_building_no")
    val subBuildingNo: String,
    
    /** 우편번호 */
    @SerializedName("zip_code")
    val zipCode: String
) 