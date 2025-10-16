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
 *
 * 성능 최적화:
 * - GPS 조회는 비용이 큰 작업이므로 30초 동안 결과를 캐싱합니다.
 * - 여러 ViewModel이 동시에 resolve()를 호출해도 GPS는 1번만 조회됩니다.
 */
class EffectiveLocationResolver @Inject constructor(
    private val selectionStore: LocationSelectionStore,
    private val locationManager: LocationManager
) {
    // 캐시된 위치 정보
    private var cachedLocation: EffectiveLocation? = null

    // 캐시 생성 시간 (밀리초)
    private var cacheTimestamp: Long = 0L

    // 캐시 유효 기간 (30초)
    private companion object {
        const val CACHE_DURATION_MS = 30_000L
    }

    /**
     * 현재 유효 위치를 계산하여 반환합니다.
     * 코루틴 컨텍스트에서 호출되어야 합니다.
     *
     * 캐싱 전략:
     * - 30초 이내에 재호출 시 캐시된 위치 반환
     * - 캐시가 만료되었거나 없으면 새로 계산
     */
    suspend fun resolve(): EffectiveLocation {
        // 캐시가 유효한지 확인
        val now = System.currentTimeMillis()
        val isCacheValid = cachedLocation != null && (now - cacheTimestamp) < CACHE_DURATION_MS

        if (isCacheValid) {
            return cachedLocation!!
        }

        // 캐시가 없거나 만료되면 새로 계산
        val location = resolveInternal()

        // 캐시 업데이트
        cachedLocation = location
        cacheTimestamp = now

        return location
    }

    /**
     * 실제 위치 해석 로직
     * 우선순위: 선택된 위치 > GPS > 기본값
     */
    private suspend fun resolveInternal(): EffectiveLocation {
        // 1순위: 사용자가 선택한 위치
        selectionStore.selectedLocation.value?.let {
            return EffectiveLocation(
                latitude = it.latitude,
                longitude = it.longitude,
                address = it.address ?: DEFAULT_ADDRESS
            )
        }

        // 2순위: GPS 위치
        val gps = locationManager.getCurrentLocation()
        if (gps != null) {
            return EffectiveLocation(
                latitude = gps.latitude,
                longitude = gps.longitude,
                address = gps.address
            )
        }

        // 3순위: 기본 위치 (서울)
        return EffectiveLocation(
            latitude = DEFAULT_LAT,
            longitude = DEFAULT_LON,
            address = DEFAULT_ADDRESS
        )
    }

    /**
     * 캐시를 즉시 무효화합니다.
     * 사용자가 수동으로 위치를 변경했을 때 호출하면 유용합니다.
     */
    fun invalidateCache() {
        cachedLocation = null
        cacheTimestamp = 0L
    }
}


