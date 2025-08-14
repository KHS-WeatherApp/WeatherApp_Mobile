package com.example.kh_studyprojects_weatherapp.data.api.kakao

import com.google.gson.annotations.SerializedName

/**
 * 카카오 로컬 API 검색 응답 모델
 * 
 * @author 개발자명
 * @since 2024.01.01
 * @version 1.0
 * 
 * 개정이력:
 * - 2024.01.01: 최초 작성
 */
data class KakaoSearchResponse(
    @SerializedName("documents")
    val documents: List<KakaoDocument>,
    @SerializedName("meta")
    val meta: KakaoMeta
)

/**
 * 카카오 검색 결과 문서
 */
data class KakaoDocument(
    @SerializedName("address_name")
    val addressName: String,
    @SerializedName("address_type")
    val addressType: String,
    @SerializedName("x")
    val longitude: String,
    @SerializedName("y")
    val latitude: String,
    @SerializedName("address")
    val address: KakaoAddress?,
    @SerializedName("road_address")
    val roadAddress: KakaoRoadAddress?
)

/**
 * 카카오 주소 정보
 */
data class KakaoAddress(
    @SerializedName("address_name")
    val addressName: String,
    @SerializedName("region_1depth_name")
    val region1depthName: String,
    @SerializedName("region_2depth_name")
    val region2depthName: String,
    @SerializedName("region_3depth_name")
    val region3depthName: String,
    @SerializedName("region_3depth_h_name")
    val region3depthHName: String,
    @SerializedName("h_code")
    val hCode: String,
    @SerializedName("b_code")
    val bCode: String,
    @SerializedName("mountain_yn")
    val mountainYn: String,
    @SerializedName("main_address_no")
    val mainAddressNo: String,
    @SerializedName("sub_address_no")
    val subAddressNo: String,
    @SerializedName("x")
    val longitude: String,
    @SerializedName("y")
    val latitude: String
)

/**
 * 카카오 도로명 주소 정보
 */
data class KakaoRoadAddress(
    @SerializedName("address_name")
    val addressName: String,
    @SerializedName("region_1depth_name")
    val region1depthName: String,
    @SerializedName("region_2depth_name")
    val region2depthName: String,
    @SerializedName("region_3depth_name")
    val region3depthName: String,
    @SerializedName("road_name")
    val roadName: String,
    @SerializedName("underground_yn")
    val undergroundYn: String,
    @SerializedName("main_building_no")
    val mainBuildingNo: String,
    @SerializedName("sub_building_no")
    val subBuildingNo: String,
    @SerializedName("building_name")
    val buildingName: String,
    @SerializedName("zone_no")
    val zoneNo: String,
    @SerializedName("x")
    val longitude: String,
    @SerializedName("y")
    val latitude: String
)

/**
 * 카카오 메타 정보
 */
data class KakaoMeta(
    @SerializedName("same_name")
    val sameName: KakaoSameName?,
    @SerializedName("pageable_count")
    val pageableCount: Int,
    @SerializedName("total_count")
    val totalCount: Int,
    @SerializedName("is_end")
    val isEnd: Boolean
)

/**
 * 카카오 동일명 정보
 */
data class KakaoSameName(
    @SerializedName("region")
    val region: List<String>,
    @SerializedName("keyword")
    val keyword: String,
    @SerializedName("selected_region")
    val selectedRegion: String
)




