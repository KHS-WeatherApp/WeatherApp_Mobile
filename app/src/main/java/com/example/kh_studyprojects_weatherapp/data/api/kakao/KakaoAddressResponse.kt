package com.example.kh_studyprojects_weatherapp.data.api.kakao

import com.google.gson.annotations.SerializedName

/**
 * 카카오 로컬 API 응답 데이터 모델
 * 
 * coord2regioncode API (위도/경도 → 주소 변환) 응답
 */
data class CoordToAddressResponse(
    /** API 응답 메타 정보 */
    @SerializedName("meta")
    val meta: Meta,
    
    /** 주소 정보 목록 (일반적으로 1개 항목) */
    @SerializedName("documents")
    val documents: List<CoordDocument>
)

/**
 * 카카오 검색 API 응답 데이터 모델
 * 
 * keyword API (주소 키워드 검색) 응답
 */
data class KeywordSearchResponse(
    /** API 응답 메타 정보 */
    @SerializedName("meta")
    val meta: SearchMeta,
    
    /** 검색 결과 목록 */
    @SerializedName("documents")
    val documents: List<SearchDocument>
)

/**
 * 공통 메타 정보
 */
data class Meta(
    /** 총 검색 결과 수 */
    @SerializedName("total_count")
    val totalCount: Int
)

/**
 * 검색 API 전용 메타 정보
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
 * coord2regioncode API (위도/경도 → 주소 변환) 응답 문서
 */
data class CoordDocument(
    /** 지역 타입 (H: 행정구역, B: 지번주소) */
    @SerializedName("region_type")
    val regionType: String,
    
    /** 행정구역 코드 */
    @SerializedName("code")
    val code: String,
    
    /** 전체 주소명 (예: 서울특별시 마포구 아현동) */
    @SerializedName("address_name")
    val addressName: String,
    
    /** 1depth 주소 (시/도) - 예: 서울특별시 */
    @SerializedName("region_1depth_name")
    val region1depthName: String,
    
    /** 2depth 주소 (구/군) - 예: 마포구 */
    @SerializedName("region_2depth_name")
    val region2depthName: String,
    
    /** 3depth 주소 (동/읍/면) - 예: 아현동 */
    @SerializedName("region_3depth_name")
    val region3depthName: String,
    
    /** 4depth 주소 (리) - 예: 아현1동 */
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
 * keyword API (주소 키워드 검색) 응답 문서
 */
data class SearchDocument(
    /** 지번주소 상세 정보 */
    @SerializedName("address")
    val address: AddressDetail?,
    
    /** 전체 주소명 (예: 서울 마포구 손기정로4안길 6) */
    @SerializedName("address_name")
    val addressName: String,
    
    /** 주소 타입 (REGION: 지번주소, ROAD_ADDR: 도로명주소) */
    @SerializedName("address_type")
    val addressType: String,
    
    /** 도로명주소 상세 정보 */
    @SerializedName("road_address")
    val roadAddress: RoadAddressDetail?,
    
    /** 경도 (x좌표) */
    @SerializedName("x")
    val x: String,
    
    /** 위도 (y좌표) */
    @SerializedName("y")
    val y: String
)

/**
 * 지번주소 상세 정보
 */
data class AddressDetail(
    /** 지번주소명 (예: 서울 마포구 아현동 713-2) */
    @SerializedName("address_name")
    val addressName: String,
    
    /** 지역 1depth (시/도) - 예: 서울 */
    @SerializedName("region_1depth_name")
    val region1depthName: String,
    
    /** 지역 2depth (구/군) - 예: 마포구 */
    @SerializedName("region_2depth_name")
    val region2depthName: String,
    
    /** 지역 3depth (동/읍/면) - 예: 아현동 */
    @SerializedName("region_3depth_name")
    val region3depthName: String,
    
    /** 지역 3depth 한글명 (예: 공덕동) */
    @SerializedName("region_3depth_h_name")
    val region3depthHName: String,
    
    /** 지번 본번 (예: 713) */
    @SerializedName("main_address_no")
    val mainAddressNo: String,
    
    /** 지번 부번 (예: 2) */
    @SerializedName("sub_address_no")
    val subAddressNo: String,
    
    /** 우편번호 */
    @SerializedName("zip_code")
    val zipCode: String,
    
    /** 경도 (x좌표) */
    @SerializedName("x")
    val x: String,
    
    /** 위도 (y좌표) */
    @SerializedName("y")
    val y: String
)

/**
 * 도로명주소 상세 정보
 */
data class RoadAddressDetail(
    /** 도로명주소명 (예: 서울 마포구 손기정로4안길 6) */
    @SerializedName("address_name")
    val addressName: String,
    
    /** 도로명 (예: 손기정로4안길) */
    @SerializedName("road_name")
    val roadName: String,
    
    /** 건물 본번 (예: 6) */
    @SerializedName("main_building_no")
    val mainBuildingNo: String,
    
    /** 건물 부번 (없을 경우 빈 문자열) */
    @SerializedName("sub_building_no")
    val subBuildingNo: String,
    
    /** 우편번호 (예: 04199) */
    @SerializedName("zone_no")
    val zoneNo: String,
    
    /** 경도 (x좌표) */
    @SerializedName("x")
    val x: String,
    
    /** 위도 (y좌표) */
    @SerializedName("y")
    val y: String
) 