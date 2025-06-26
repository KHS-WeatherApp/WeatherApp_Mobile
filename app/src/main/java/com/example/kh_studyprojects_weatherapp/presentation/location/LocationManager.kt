package com.example.kh_studyprojects_weatherapp.presentation.location

/**
 *################################################################################
 * <p> Google Play Services Location API를 사용한 위치 정보 관리 클래스 GPS </p>
 * 
 * FusedLocationProviderClient를 사용하여 GPS, 네트워크, 센서 등 여러 소스의 위치 정보를
 * 결합하여 정확한 위치 정보를 제공합니다.
 *
 * @author 김효동
 * @since 2025.05.18
 * @version 1.0
 * @see
 *
 * <pre>
 * << 개정이력(Modification Information) >>
 *
 * 수정일		수정자	수정내용
 * ----------	------	---------------------------
 * 2025.05.18	김효동	최초 생성
 * </pre>
 *################################################################################
 */

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class LocationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val geocoder: Geocoder
) {
    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    // 위치 정보를 담는 데이터 클래스
    data class LocationInfo(
        val latitude: Double,   // 위도   
        val longitude: Double,  // 경도
        val address: String     // 주소
    )

    // 위치 권한 확인
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

    // 현재 위치 정보를 가져오는 함수
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
                // 위치 정보로부터 주소 가져오기
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

    // 마지막으로 알려진 위치를 가져오는 함수
    private suspend fun getLastLocation(): Location? = suspendCancellableCoroutine { continuation ->
        try {
            val cancellationToken = CancellationTokenSource()
            val addOnFailureListener = fusedLocationClient.getCurrentLocation(
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

    // 위도/경도로부터 주소를 가져오는 함수
    private fun getAddressFromLocation(latitude: Double, longitude: Double): String {
        return try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            val address = addresses?.firstOrNull()
            address?.let {
                val adminArea = it.adminArea ?: ""          // 시/도
                val subLocality = it.subLocality ?: ""      // 구/군
                val thoroughfare = it.thoroughfare ?: ""    // 동/읍/면
                
                // 주소 조합
                when {
                    adminArea.isNotEmpty() && subLocality.isNotEmpty() && thoroughfare.isNotEmpty() -> 
                        "$adminArea $subLocality $thoroughfare"
                    adminArea.isNotEmpty() && subLocality.isNotEmpty() -> 
                        "$adminArea $subLocality"
                    else -> it.getAddressLine(0) ?: "알 수 없는 위치"
                }
            } ?: "알 수 없는 위치"
        } catch (e: Exception) {
            Log.e("LocationManager", "주소 변환 중 오류 발생", e)
            "알 수 없는 위치"
        }
    }
} 