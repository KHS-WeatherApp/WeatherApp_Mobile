package com.example.kh_studyprojects_weatherapp.data.api.sidemenu

import retrofit2.Response
import retrofit2.http.*

/**
 * 사이드메뉴 즐겨찾기 지역 API 서비스
 * 
 * 즐겨찾기 지역 추가, 삭제, 조회 등의 API를 정의합니다.
 *
 * @author 김효동
 * @since 2025.08.26
 * @version 1.0
 */
interface SmFavoriteLocationApiService {
    
    /**
     * 즐겨찾기 지역 목록 조회
     */
    @GET("/api/sidemenu/locations")
    suspend fun getFavoriteLocations(@Query("deviceId") deviceId: String): Response<List<SmFavoriteLocationResponse>>
    
    /**
     * 즐겨찾기 지역 추가
     */
    @POST("/api/sidemenu/locations")
    suspend fun addFavoriteLocation(
        @Body request: SmFavoriteLocationRequest
    ): Response<SmFavoriteLocationResponse>
    
    /**
     * 즐겨찾기 지역 삭제
     */
    @DELETE("/api/sidemenu/locations")
    suspend fun deleteFavoriteLocation(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("deviceId") deviceId: String
    ): Response<Unit>
    
    /**
     * 즐겨찾기 지역 정렬 순서 수정
     */
    @PATCH("/api/sidemenu/locations/sort-order")
    suspend fun updateFavoriteLocationSortOrder(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("deviceId") deviceId: String,
        @Query("sortOrder") sortOrder: Int
    ): Response<Unit>
}

