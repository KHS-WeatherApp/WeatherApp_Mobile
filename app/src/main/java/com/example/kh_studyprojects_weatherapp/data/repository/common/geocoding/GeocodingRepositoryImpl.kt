package com.example.kh_studyprojects_weatherapp.data.repository.common.geocoding

import com.example.kh_studyprojects_weatherapp.data.api.kakao.CoordToAddressResponse
import com.example.kh_studyprojects_weatherapp.data.api.kakao.KakaoApiService
import com.example.kh_studyprojects_weatherapp.data.api.kakao.KeywordSearchResponse
import com.example.kh_studyprojects_weatherapp.domain.repository.common.geocoding.GeocodingRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * GeocodingRepository 구현체
 * - 카카오 로컬 API 클라이언트를 주입 받아 호출을 위임합니다.
 */
@Singleton
class GeocodingRepositoryImpl @Inject constructor(
    private val kakaoApiService: KakaoApiService
) : GeocodingRepository {
    override suspend fun searchByAddress(query: String, page: Int, size: Int): KeywordSearchResponse =
        kakaoApiService.searchByAddress(query = query, page = page, size = size)

    override suspend fun getAddressFromCoordinates(longitude: String, latitude: String): CoordToAddressResponse =
        kakaoApiService.getAddressFromCoordinates(longitude = longitude, latitude = latitude)
}
