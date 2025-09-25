package com.example.kh_studyprojects_weatherapp.presentation.common.location

import javax.inject.Inject
import javax.inject.Singleton

// 기본 위도(서울)
private const val DEFAULT_LAT = 37.5665
// 기본 경도(서울)
private const val DEFAULT_LON = 126.9780
// 기본 주소 레이블
private const val DEFAULT_ADDRESS = "기본 위치(서울)"

/**
 * 화면에 표시할 최종 위치 정보를 나타냅니다.
 * latitude(위도), longitude(경도), address(주소 문자열)를 포함합니다.
 */
data class EffectiveLocation(
    val latitude: Double,
    val longitude: Double,
    val address: String
)

@Singleton
/**
 * 선택된 위치, GPS, 기본값을 우선순위로 고려해 '유효 위치'를 계산합니다.
 *
 * 우선순위
 * 1) 사용자가 선택한 위치
 * 2) 기기 GPS 위치
 * 3) 기본 위치 상수
 */
class EffectiveLocationResolver @Inject constructor(
    private val selectionStore: LocationSelectionStore,
    private val locationManager: LocationManager
) {
    /**
     * 현재 유효 위치를 계산하여 반환합니다.
     * 코루틴 컨텍스트에서 호출되어야 합니다.
     */
    suspend fun resolve(): EffectiveLocation {
        selectionStore.selectedLocation.value?.let {
            return EffectiveLocation(
                latitude = it.latitude,
                longitude = it.longitude,
                address = it.address ?: DEFAULT_ADDRESS
            )
        }

        val gps = locationManager.getCurrentLocation()
        if (gps != null) {
            return EffectiveLocation(
                latitude = gps.latitude,
                longitude = gps.longitude,
                address = gps.address
            )
        }

        return EffectiveLocation(
            latitude = DEFAULT_LAT,
            longitude = DEFAULT_LON,
            address = DEFAULT_ADDRESS
        )
    }
}


