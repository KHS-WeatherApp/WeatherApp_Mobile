package com.example.kh_studyprojects_weatherapp.domain.repository.common.sidemenu

import com.example.kh_studyprojects_weatherapp.domain.model.location.FavoriteLocation

/**
 * 즐겨찾기 지역 Repository 인터페이스
 *
 * 즐겨찾기 지역 데이터의 CRUD 작업을 정의합니다.
 * 백엔드 서버와의 통신을 통해 데이터를 관리합니다.
 *
 * @author 김효동
 * @since 2025.08.26
 * @version 2.0 (Result 타입으로 변경)
 */
interface SmFavoriteLocationRepository {

    /**
     * 디바이스의 즐겨찾기 지역 목록을 조회합니다.
     *
     * @param deviceId 디바이스 고유 식별자
     * @return Result<List<FavoriteLocation>> 성공 시 목록, 실패 시 예외
     */
    suspend fun getFavoriteLocations(deviceId: String): Result<List<FavoriteLocation>>

    /**
     * 새로운 즐겨찾기 지역을 추가합니다.
     *
     * @param location 추가할 즐겨찾기 지역 정보
     * @return Result<String> 성공 시 메시지, 실패 시 예외
     */
    suspend fun addFavoriteLocation(location: FavoriteLocation): Result<String>

    /**
     * 즐겨찾기 지역을 삭제합니다.
     *
     * @param latitude 삭제할 즐겨찾기 지역의 위도
     * @param longitude 삭제할 즐겨찾기 지역의 경도
     * @param deviceId 디바이스 고유 식별자
     * @return Result<String> 성공 시 메시지, 실패 시 예외
     */
    suspend fun deleteFavoriteLocation(
        latitude: Double,
        longitude: Double,
        deviceId: String
    ): Result<String>

    /**
     * 즐겨찾기 지역의 정렬 순서를 업데이트합니다.
     *
     * @param latitude 업데이트할 즐겨찾기 지역의 위도
     * @param longitude 업데이트할 즐겨찾기 지역의 경도
     * @param deviceId 디바이스 고유 식별자
     * @param sortOrder 새로운 정렬 순서
     * @return Result<String> 성공 시 메시지, 실패 시 예외
     */
    suspend fun updateSortOrder(
        latitude: Double,
        longitude: Double,
        deviceId: String,
        sortOrder: Int
    ): Result<String>
}

