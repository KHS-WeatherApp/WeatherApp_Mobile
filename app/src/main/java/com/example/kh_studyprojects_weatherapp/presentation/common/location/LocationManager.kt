package com.example.kh_studyprojects_weatherapp.presentation.common.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.core.app.ActivityCompat
import com.example.kh_studyprojects_weatherapp.data.api.kakao.KakaoApiService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
/**
 * LocationManager
 * - FusedLocationProviderClient로 현재 좌표를 얻고, Kakao 로컬 API로 주소 문자열을 조회합니다.
 * - 프레젠테이션 계층에서 위치/주소 조회를 단일 진입점으로 제공합니다.
 */
class LocationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val kakaoApiService: KakaoApiService
) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    data class LocationInfo(
        val latitude: Double,
        val longitude: Double,
        val address: String
    )

    /** 위치 권한 체크 */
    fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
    }

    /** 현재 위치 조회 + 주소 변환 */
    suspend fun getCurrentLocation(): LocationInfo? {
        return try {
            if (!hasLocationPermission()) {
                Log.w("LocationManager", "위치 권한이 없습니다.")
                return null
            }
            val location = getLastLocation()
            if (location != null) {
                val address = getAddressFromLocation(location.latitude, location.longitude)
                LocationInfo(location.latitude, location.longitude, address)
            } else {
                Log.w("LocationManager", "위치 정보를 가져올 수 없습니다.")
                null
            }
        } catch (e: Exception) {
            Log.e("LocationManager", "위치 정보를 가져오는 중 오류 발생", e)
            null
        }
    }

    /** 마지막/현재 위치 한 번 조회 (고정 수집) */
    private suspend fun getLastLocation(): Location? = suspendCancellableCoroutine { continuation ->
        try {
            if (!hasLocationPermission()) {
                Log.w("LocationManager", "위치 권한이 없습니다.")
                continuation.resume(null)
                return@suspendCancellableCoroutine
            }
            val cancellationToken = CancellationTokenSource()
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationToken.token
            ).addOnSuccessListener { location ->
                continuation.resume(location)
            }.addOnFailureListener { e ->
                Log.e("LocationManager", "위치 정보를 가져오는데 실패했습니다.", e)
                continuation.resume(null)
            }
        } catch (e: Exception) {
            Log.e("LocationManager", "위치 정보를 가져오는 중 오류 발생", e)
            continuation.resume(null)
        }
    }

    /** 좌표 -> 주소 변환 (카카오 로컬 API) */
    private suspend fun getAddressFromLocation(latitude: Double, longitude: Double): String {
        return try {
            val response = kakaoApiService.getAddressFromCoordinates(
                longitude = longitude.toString(),
                latitude = latitude.toString()
            )
            
            // 가장 상세한 행정구역 정보 조합
            val document = response.documents.firstOrNull()
            document?.let {
                val region1 = it.region1depthName
                val region2 = it.region2depthName
                val region3 = it.region3depthName
                when {
                    region1.isNotEmpty() && region2.isNotEmpty() && region3.isNotEmpty() -> "$region1 $region2 $region3"
                    region1.isNotEmpty() && region2.isNotEmpty() -> "$region1 $region2"
                    else -> it.addressName
                }
            } ?: "알 수 없는 위치"
        } catch (e: Exception) {
            Log.e("LocationManager", "카카오 API 주소 변환 중 오류 발생", e)
            Log.e("LocationManager", "위도$latitude/경도$longitude")
            "알 수 없는 위치"
        }
    }
}
