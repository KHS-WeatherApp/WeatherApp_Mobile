package com.example.kh_studyprojects_weatherapp.data.repository.common.sidemenu

import android.util.Log
import com.example.kh_studyprojects_weatherapp.data.api.ApiServiceProvider
import com.example.kh_studyprojects_weatherapp.data.api.sidemenu.SmFavoriteLocationRequest
import com.example.kh_studyprojects_weatherapp.domain.model.location.FavoriteLocation
import com.example.kh_studyprojects_weatherapp.domain.repository.common.sidemenu.SmFavoriteLocationRepository
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 사이드메뉴 통합 Repository
 * 
 * 기존의 SmFavoriteLocationRepository와 SmFavoriteLocationRepositoryImpl을 통합
 * 백엔드 서버와의 통신을 통해 즐겨찾기 지역 데이터를 관리합니다.
 *
 * @author 김효동
 * @since 2025.08.26
 * @version 2.0 (통합 버전)
 */
@Singleton
class SmRepository @Inject constructor() : SmFavoriteLocationRepository {

    private val apiService = ApiServiceProvider.smFavoriteLocationApiService

    override suspend fun getFavoriteLocations(deviceId: String): List<FavoriteLocation>? {
        return try {
            Log.d("SmRepository", "즐겨찾기 지역 목록 조회 시작: deviceId=$deviceId")
            
            val response = apiService.getFavoriteLocations(deviceId)
            val locations = response.body()?.map { dto ->
                FavoriteLocation(
                    deviceId = dto.deviceId,
                    latitude = dto.latitude,
                    longitude = dto.longitude,
                    addressName = dto.addressName,
                    region1depthName = dto.region1DepthName ?: "",
                    region2depthName = dto.region2DepthName ?: "",
                    region3depthName = dto.region3DepthName ?: "",
                    sortOrder = dto.sortOrder
                )
            } ?: emptyList()
            
            Log.d("SmRepository", "즐겨찾기 지역 목록 조회 성공: ${locations.size}개")
            locations
            
        } catch (e: HttpException) {
            val code = e.code()
            val msg = e.response()?.errorBody()?.string().orEmpty()
            Log.e("SmRepository", "HTTP 오류 $code: $msg", e)
            null
        } catch (e: IOException) {
            Log.e("SmRepository", "네트워크 오류", e)
            null
        } catch (e: Exception) {
            Log.e("SmRepository", "기타 오류", e)
            null
        }
    }

    override suspend fun addFavoriteLocation(location: FavoriteLocation): Boolean {
        return try {
            Log.d("SmRepository", "즐겨찾기 지역 추가 시작: ${location.addressName}")
            
            val request = SmFavoriteLocationRequest(
                addressName = location.addressName,
                latitude = location.latitude,
                longitude = location.longitude,
                region1DepthName = location.region1depthName ?: "",
                region2DepthName = location.region2depthName ?: "",
                region3DepthName = location.region3depthName ?: "",
                deviceId = location.deviceId,
                sortOrder = location.sortOrder
            )
            
            val response = apiService.addFavoriteLocation(request)
            Log.d("SmRepository", "즐겨찾기 지역 추가 성공: addressName=${response.body()?.addressName}")
            true
            
        } catch (e: HttpException) {
            val code = e.code()
            val msg = e.response()?.errorBody()?.string().orEmpty()
            Log.e("SmRepository", "HTTP 오류 $code: $msg", e)
            false
        } catch (e: IOException) {
            Log.e("SmRepository", "네트워크 오류", e)
            false
        } catch (e: Exception) {
            Log.e("SmRepository", "기타 오류", e)
            false
        }
    }

    override suspend fun deleteFavoriteLocation(
        latitude: Double,
        longitude: Double,
        deviceId: String
    ): Boolean {
        return try {
            Log.d("SmRepository", "즐겨찾기 지역 삭제 시작: lat=$latitude, lng=$longitude, deviceId=$deviceId")
            val result = apiService.deleteFavoriteLocation(latitude, longitude, deviceId)
            Log.d("SmRepository", "즐겨찾기 지역 삭제 결과: ${result.isSuccessful}")
            result.isSuccessful
        } catch (e: HttpException) {
            val code = e.code()
            val msg = e.response()?.errorBody()?.string().orEmpty()
            Log.e("SmRepository", "HTTP 오류 $code: $msg", e)
            false
        } catch (e: IOException) {
            Log.e("SmRepository", "네트워크 오류", e)
            false
        } catch (e: Exception) {
            Log.e("SmRepository", "기타 오류", e)
            false
        }
    }

    override suspend fun updateSortOrder(
        latitude: Double,
        longitude: Double,
        deviceId: String,
        sortOrder: Int
    ): Boolean {
        return try {
            Log.d("SmRepository", "정렬 순서 업데이트 시작: lat=$latitude, lng=$longitude, deviceId=$deviceId, sortOrder=$sortOrder")
            val response = apiService.updateFavoriteLocationSortOrder(latitude, longitude, deviceId, sortOrder)
            Log.d("SmRepository", "정렬 순서 업데이트 성공: ${response.isSuccessful}")
            response.isSuccessful
        } catch (e: HttpException) {
            val code = e.code()
            val msg = e.response()?.errorBody()?.string().orEmpty()
            Log.e("SmRepository", "HTTP 오류 $code: $msg", e)
            false
        } catch (e: IOException) {
            Log.e("SmRepository", "네트워크 오류", e)
            false
        } catch (e: Exception) {
            Log.e("SmRepository", "기타 오류", e)
            false
        }
    }
}
