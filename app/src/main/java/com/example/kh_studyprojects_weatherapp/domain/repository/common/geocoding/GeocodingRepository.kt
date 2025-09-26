package com.example.kh_studyprojects_weatherapp.domain.repository.common.geocoding

import com.example.kh_studyprojects_weatherapp.data.api.kakao.CoordToAddressResponse
import com.example.kh_studyprojects_weatherapp.data.api.kakao.KeywordSearchResponse

/**
 * GeocodingRepository
 * - 카카오 로컬 API를 사용한 지오코딩(주소 검색 / 좌표→주소 변환)을 캡슐화합니다.
 * - 프레젠테이션 계층에서 Retrofit/키를 직접 다루지 않도록 하기 위한 도메인 레이어 인터페이스입니다.
 */
interface GeocodingRepository {
    /** 주소 검색 */
    suspend fun searchByAddress(query: String, page: Int = 1, size: Int = 30): KeywordSearchResponse

    /** 좌표 → 주소 변환 */
    suspend fun getAddressFromCoordinates(longitude: String, latitude: String): CoordToAddressResponse
}
