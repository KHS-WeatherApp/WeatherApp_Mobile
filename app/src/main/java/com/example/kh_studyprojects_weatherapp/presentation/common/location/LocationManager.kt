package com.example.kh_studyprojects_weatherapp.presentation.common.location

/**
 *################################################################################
 * <p> 위치 정보 관리 클래스 - GPS + 카카오 로컬 API </p>
 * 
 * FusedLocationProviderClient를 사용하여 GPS, 네트워크, 센서 등 여러 소스의 위치 정보를
 * 결합하여 정확한 위치 정보를 제공하고, 카카오 로컬 API를 사용하여 위도/경도를 주소로 변환합니다.
 * 
 * 기존 Google Geocoder 대신 카카오 로컬 API를 사용하여 더 정확한 한국 주소 정보를 제공합니다.
 *
 * @author 김효동
 * @since 2025.2025.08.06
 * @version 2.0 (카카오 API 적용)
 * @see
 *
 * <pre>
 * << 개정이력(Modification Information) >>
 *
 * 수정일		수정자	수정내용
 * ----------	------	---------------------------
 * 2025.05.18	김효동	최초 생성 (Google Geocoder 사용)
 * 2025.08.06	김효동	카카오 로컬 API 적용 (Google Geocoder 제거)
 * </pre>
 *################################################################################
 */

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.core.app.ActivityCompat
import com.example.kh_studyprojects_weatherapp.data.api.ApiServiceProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class LocationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /** Google Play Services 위치 제공자 클라이언트 */
    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    
    /** 카카오 로컬 API 서비스 */
    private val kakaoApiService = ApiServiceProvider.kakaoApiService

    /**
     * 위치 정보를 담는 데이터 클래스
     * 
     * @param latitude 위도
     * @param longitude 경도
     * @param address 카카오 API로 변환된 한국어 주소
     */
    data class LocationInfo(
        val latitude: Double,   // 위도   
        val longitude: Double,  // 경도
        val address: String     // 주소 (카카오 API로 변환)
    )

    /**
     * 위치 권한 확인
     * 
     * @return true: 위치 권한 있음, false: 위치 권한 없음
     */
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

    /**
     * 현재 위치 정보를 가져오는 함수
     * 
     * GPS로 위도/경도를 획득하고, 카카오 로컬 API를 사용하여 주소로 변환합니다.
     * 
     * @return LocationInfo? 위치 정보 (실패 시 null)
     */
    suspend fun getCurrentLocation(): LocationInfo? {
        return try {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.w("LocationManager", "위치 권한이 없습니다.")
                return null
            }

            val location = getLastLocation()
            if (location != null) {
                // 카카오 API로 주소 가져오기
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

    /**
     * 마지막으로 알려진 위치를 가져오는 함수
     * 
     * Google Play Services의 FusedLocationProviderClient를 사용하여
     * GPS, 네트워크, 센서 등 여러 소스의 위치 정보를 결합하여 정확한 위치를 제공합니다.
     * 
     * @return Location? 위치 정보 (실패 시 null)
     */
    private suspend fun getLastLocation(): Location? = suspendCancellableCoroutine { continuation ->
        try {
            // 권한 체크 추가
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
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

    /**
     * 카카오 로컬 API를 사용하여 위도/경도로부터 주소를 가져오는 함수
     * 
     * 기존 Google Geocoder 대신 카카오 로컬 API를 사용하여 더 정확한 한국 주소 정보를 제공합니다.
     * 시/도, 구/군, 동/읍/면을 조합하여 사용자 친화적인 주소를 생성합니다.
     * 
     * @param latitude 위도
     * @param longitude 경도
     * @return String 변환된 주소 (실패 시 "알 수 없는 위치")
     */
    private suspend fun getAddressFromLocation(latitude: Double, longitude: Double): String {
        return try {
            val response = kakaoApiService.getAddressFromCoordinates(
                longitude = longitude.toString(),
                latitude = latitude.toString()
            )
            
            val document = response.documents.firstOrNull()
            document?.let {
                // 카카오 API 응답에서 주소 조합
                val region1 = it.region1depthName // 시/도
                val region2 = it.region2depthName // 구/군
                val region3 = it.region3depthName // 동/읍/면
                
                when {
                    region1.isNotEmpty() && region2.isNotEmpty() && region3.isNotEmpty() -> 
                        "$region1 $region2 $region3"
                    region1.isNotEmpty() && region2.isNotEmpty() -> 
                        "$region1 $region2"
                    else -> it.addressName
                }
            } ?: "알 수 없는 위치"
        } catch (e: Exception) {
            Log.e("LocationManager", "카카오 API 주소 변환 중 오류 발생", e)
            "알 수 없는 위치"
        }
    }
} 