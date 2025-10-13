package com.example.kh_studyprojects_weatherapp.data.repository.common.sidemenu

import android.util.Log
import com.example.kh_studyprojects_weatherapp.data.api.sidemenu.SmFavoriteLocationApiService
import com.example.kh_studyprojects_weatherapp.data.api.sidemenu.SmFavoriteLocationRequest
import com.example.kh_studyprojects_weatherapp.domain.model.location.FavoriteLocation
import com.example.kh_studyprojects_weatherapp.data.api.common.ApiResponse
import com.example.kh_studyprojects_weatherapp.data.repository.base.BaseRepository
import com.example.kh_studyprojects_weatherapp.domain.repository.common.sidemenu.SmFavoriteLocationRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 사이드메뉴 통합 Repository
 *
 * BaseRepository를 상속하여 통일된 에러 처리를 사용합니다.
 * 백엔드 서버와의 통신을 통해 즐겨찾기 지역 데이터를 관리합니다.
 *
 * @author 김효동
 * @since 2025.08.26
 * @version 3.0 (BaseRepository 상속, Result 타입 사용)
 */
@Singleton
class SmRepository @Inject constructor(
    private val apiService: SmFavoriteLocationApiService
) : BaseRepository(), SmFavoriteLocationRepository {

    override val TAG = "SmRepository"

    // 즐겨찾기 지역 목록 조회
    override suspend fun getFavoriteLocations(deviceId: String): Result<List<FavoriteLocation>> {
        return safeApiCallWithTransform(
            apiCall = { apiService.getFavoriteLocations(deviceId) },
            transform = { apiResponse ->
                apiResponse.data?.map { dto ->
                    FavoriteLocation(
                        deviceId = dto.deviceId,
                        latitude = dto.latitude,
                        longitude = dto.longitude,
                        addressName = dto.addressName,
                        region1depthName = dto.region1DepthName?.takeIf { it.isNotBlank() },
                        region2depthName = dto.region2DepthName?.takeIf { it.isNotBlank() },
                        region3depthName = dto.region3DepthName?.takeIf { it.isNotBlank() },
                        region3depthHName = dto.region3DepthHName?.takeIf { it.isNotBlank() },
                        sortOrder = dto.sortOrder
                    )
                } ?: emptyList()
            }
        )
    }
    
    // 즐겨찾기 지역 추가
    override suspend fun addFavoriteLocation(location: FavoriteLocation): Result<String> {
        val request = SmFavoriteLocationRequest(
            addressName = location.addressName,
            latitude = location.latitude,
            longitude = location.longitude,
            region1DepthName = location.region1depthName ?: "",
            region2DepthName = location.region2depthName ?: "",
            region3DepthName = location.region3depthName ?: "",
            region3DepthHName = location.region3depthHName ?: "",
            deviceId = location.deviceId,
            sortOrder = location.sortOrder
        )

        return safeApiCallWithTransform(
            apiCall = { apiService.addFavoriteLocation(request) },
            transform = { apiResponse -> apiResponse.message }
        )
    }

    // 즐겨찾기 지역 삭제
    override suspend fun deleteFavoriteLocation(
        latitude: Double,
        longitude: Double,
        deviceId: String
    ): Result<String> {
        return safeApiCallWithTransform(
            apiCall = { apiService.deleteFavoriteLocation(latitude, longitude, deviceId) },
            transform = { apiResponse -> apiResponse.message }
        )
    }

    // 즐겨찾기 지역 정렬 순서 변경
    override suspend fun updateSortOrder(
        latitude: Double,
        longitude: Double,
        deviceId: String,
        sortOrder: Int
    ): Result<String> {
        return safeApiCallWithTransform(
            apiCall = { apiService.updateFavoriteLocationSortOrder(latitude, longitude, deviceId, sortOrder) },
            transform = { apiResponse -> apiResponse.message }
        )
    }
}
