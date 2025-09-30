package com.example.kh_studyprojects_weatherapp.data.api.weather
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * 날씨 API 서비스 인터페이스
 *
 * 백엔드 서버와 통신하여 날씨 정보와 대기질 정보를 가져오는 API 클라이언트입니다.
 * Retrofit을 사용하여 구현되며, POST 요청을 통해 위도/경도 기반 데이터를 조회합니다.
 *
 * @author 김지윤
 * @since 2024.01.01
 * @version 1.0
 */
interface WeatherApiService {
    /**
     * 날씨 정보 조회
     *
     * 위도/경도를 포함한 WeatherRequest를 서버에 전송하여
     * 현재 날씨, 시간별 예보, 일별 예보 등의 정보를 받아옵니다.
     *
     * @param request 위도, 경도, 쿼리 파라미터를 포함한 요청 객체
     * @return Response<Map<String, Any>> 날씨 데이터가 담긴 서버 응답
     */
    @POST("/api/weather")
    suspend fun getWeatherInfo(
        @Body request: WeatherRequest
    ): Response<Map<String, Any>>

    /**
     * 대기질 정보 조회
     *
     * 위도/경도를 포함한 WeatherRequest를 서버에 전송하여
     * PM2.5, PM10, UV 지수 등의 대기질 관련 정보를 받아옵니다.
     *
     * @param request 위도, 경도, 쿼리 파라미터를 포함한 요청 객체
     * @return Response<Map<String, Any>> 대기질 데이터가 담긴 서버 응답
     * @author 이수연
     * @since 2025.04.27
     */
    @POST("/api/airPollution")
    suspend fun getAdditionalWeatherInfo(
        @Body request: WeatherRequest
    ): Response<Map<String, Any>>
}