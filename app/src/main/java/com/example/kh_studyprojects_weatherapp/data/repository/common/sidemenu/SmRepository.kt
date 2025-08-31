package com.example.kh_studyprojects_weatherapp.data.repository.common.sidemenu

import android.util.Log
import com.example.kh_studyprojects_weatherapp.data.api.ApiServiceProvider
import com.example.kh_studyprojects_weatherapp.data.api.common.ApiResponse
import com.example.kh_studyprojects_weatherapp.data.api.sidemenu.SmFavoriteLocationRequest
import com.example.kh_studyprojects_weatherapp.domain.model.location.FavoriteLocation
import com.example.kh_studyprojects_weatherapp.domain.repository.common.sidemenu.SmFavoriteLocationRepository
import retrofit2.HttpException
import org.json.JSONObject
import org.json.JSONException
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
            val response = apiService.getFavoriteLocations(deviceId)

            if (!response.isSuccessful) {
                Log.e("SmRepository", "API 호출 실패: ${response.code()}")
                return null
            }
            
            val responseBody = response.body()
            if (responseBody == null) {
                return null
            }
            
            val locations = responseBody.data?.map { dto ->
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
            }
            
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
            Log.e("SmRepository", "기타 오류: ${e.message}", e)
            Log.e("SmRepository", "오류 스택 트레이스", e)
            null
        }
    }

    override suspend fun addFavoriteLocation(location: FavoriteLocation): Pair<Boolean, String> {
        return try {
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
            
            val response = apiService.addFavoriteLocation(request)
            if (response.isSuccessful) {
                val body = response.body()
                // ApiResponse에서 message 추출
                val message = body?.message ?: "${location.addressName}이(가) 즐겨찾기에 추가되었습니다"
                Pair(true, message)
            } else {
                val errorBody = response.errorBody()?.string().orEmpty()
                val msg = extractServerMessage(errorBody).ifBlank { response.message().orEmpty() }
                Pair(false, if (msg.isNotBlank()) msg else "즐겨찾기 추가에 실패했습니다.")
            }
            
        } catch (e: HttpException) {
            val code = e.code()
            val msg = e.response()?.errorBody()?.string().orEmpty()
            Log.e("SmRepository", "HTTP 오류 $code", e)
            
            val parsed = extractServerMessage(msg)
            Pair(false, if (parsed.isNotBlank()) parsed else "즐겨찾기 추가에 실패했습니다.")
        } catch (e: IOException) {
            Log.e("SmRepository", "네트워크 오류", e)
            Pair(false, "네트워크 오류가 발생했습니다.")
        } catch (e: Exception) {
            Log.e("SmRepository", "기타 오류", e)
            Pair(false, "즐겨찾기 추가 중 오류가 발생했습니다.")
        }
    }

    private fun extractServerMessage(errorBody: String): String {
        if (errorBody.isBlank()) return ""
        try {
            val json = JSONObject(errorBody)
            
            val message = json.optString("message")
            if (message.isNotBlank()) {
                return message
            }
            
            if (json.has("error")) {
                val err = json.opt("error")
                if (err is JSONObject) {
                    val nested = json.optString("message")
                    if (nested.isNotBlank()) return nested
                }
            }
        } catch (e: JSONException) {
            // JSON 파싱 실패 시 원본 텍스트 반환
        }
        return errorBody.trim()
    }

    override suspend fun deleteFavoriteLocation(
        latitude: Double,
        longitude: Double,
        deviceId: String
    ): Pair<Boolean, String> {
        return try {
            val result = apiService.deleteFavoriteLocation(latitude, longitude, deviceId)
            
            if (result.isSuccessful) {
                val body = result.body()
                val message = body?.message ?: "즐겨찾기가 삭제되었습니다"
                Pair(true, message)
            } else {
                val errorBody = result.errorBody()?.string().orEmpty()
                val msg = extractServerMessage(errorBody).ifBlank { result.message().orEmpty() }
                Pair(false, if (msg.isNotBlank()) msg else "즐겨찾기 삭제에 실패했습니다.")
            }
            
        } catch (e: HttpException) {
            val code = e.code()
            val msg = e.response()?.errorBody()?.string().orEmpty()
            Log.e("SmRepository", "HTTP 오류 $code", e)
            
            val parsed = extractServerMessage(msg)
            Pair(false, if (parsed.isNotBlank()) parsed else "즐겨찾기 삭제에 실패했습니다.")
        } catch (e: IOException) {
            Log.e("SmRepository", "네트워크 오류", e)
            Pair(false, "네트워크 오류가 발생했습니다.")
        } catch (e: Exception) {
            Log.e("SmRepository", "기타 오류", e)
            Pair(false, "즐겨찾기 삭제 중 오류가 발생했습니다.")
        }
    }

    override suspend fun updateSortOrder(
        latitude: Double,
        longitude: Double,
        deviceId: String,
        sortOrder: Int
    ): Pair<Boolean, String> {
        return try {
            val response = apiService.updateFavoriteLocationSortOrder(latitude, longitude, deviceId, sortOrder)
            
            if (response.isSuccessful) {
                val body = response.body()
                val message = body?.message ?: "정렬 순서가 변경되었습니다"
                Pair(true, message)
            } else {
                val errorBody = response.errorBody()?.string().orEmpty()
                val msg = extractServerMessage(errorBody).ifBlank { response.message().orEmpty() }
                Pair(false, if (msg.isNotBlank()) msg else "정렬 순서 변경에 실패했습니다.")
            }
            
        } catch (e: HttpException) {
            val code = e.code()
            val msg = e.response()?.errorBody()?.string().orEmpty()
            Log.e("SmRepository", "HTTP 오류 $code", e)
            
            val parsed = extractServerMessage(msg)
            Pair(false, if (parsed.isNotBlank()) parsed else "정렬 순서 변경에 실패했습니다.")
        } catch (e: IOException) {
            Log.e("SmRepository", "네트워크 오류", e)
            Pair(false, "네트워크 오류가 발생했습니다.")
        } catch (e: Exception) {
            Log.e("SmRepository", "기타 오류", e)
            Pair(false, "정렬 순서 변경 중 오류가 발생했습니다.")
        }
    }
}
