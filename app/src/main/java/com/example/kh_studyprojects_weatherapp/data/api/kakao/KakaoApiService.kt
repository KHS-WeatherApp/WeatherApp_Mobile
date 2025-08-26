package com.example.kh_studyprojects_weatherapp.data.api.kakao

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * 카카오 로컬 API 서비스 인터페이스
 * 
 * 위도/경도 좌표를 주소로 변환하는 카카오 로컬 API를 호출하는 인터페이스입니다.
 * 기존 Google Geocoder 대신 카카오 API를 사용하여 더 정확한 한국 주소 정보를 제공합니다.
 *
 * @author 김효동
 * @since 2025.08.06
 * @version 1.0
 * 
 * @see <a href="https://developers.kakao.com/docs/latest/ko/local/dev-guide#coord-to-address">카카오 로컬 API 문서</a>
 */
interface KakaoApiService {
    
    /**
     * 위도/경도 좌표를 주소로 변환하는 API
     * 
     * WGS84 좌표계의 위도/경도를 받아서 해당 위치의 주소 정보를 반환합니다.
     * 카카오 로컬 API의 coord2regioncode 엔드포인트를 사용합니다.
     *
     * @param inputCoord 입력 좌표계 (기본값: WGS84)
     * @param outputCoord 출력 좌표계 (기본값: WGS84)
     * @param longitude 경도 (x좌표)
     * @param latitude 위도 (y좌표)
     * @return KakaoAddressResponse 카카오 API 응답 데이터
     * 
     * @example
     */
    @GET("v2/local/geo/coord2regioncode.json")
    suspend fun getAddressFromCoordinates(
        @Query("input_coord") inputCoord: String = "WGS84",
        @Query("output_coord") outputCoord: String = "WGS84",
        @Query("x") longitude: String,
        @Query("y") latitude: String
    ): CoordToAddressResponse

    /**
     * 주소로 좌표 변환 검색 (페이지네이션 지원)
     * @param query 검색어
     * @param page 페이지 번호(1부터)
     * @param size 페이지 당 개수(최대 30)
     */
    @GET("v2/local/search/address.json")
    suspend fun searchByAddress(
        @Query("query") query: String,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 30
    ): KeywordSearchResponse
} 