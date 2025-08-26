package com.example.kh_studyprojects_weatherapp.data.api

import com.example.kh_studyprojects_weatherapp.data.api.weather.WeatherApiService
import com.example.kh_studyprojects_weatherapp.data.api.sidemenu.SmFavoriteLocationApiService
import com.example.kh_studyprojects_weatherapp.data.api.kakao.KakaoApiService

/**
 * 통합 API 서비스 제공자
 * 
 * 백엔드 API와 외부 API의 모든 서비스 인스턴스들을 제공합니다.
 * BackendRetrofitConfig와 ExternalRetrofitConfig의 retrofit을 사용하여 각 API 서비스를 생성합니다.
 *
 * @author 김효동
 * @since 2025.08.26
 * @version 1.0
 */
object ApiServiceProvider {
    
    // ===== 백엔드 API 서비스들 =====
    
    /** 날씨 API 서비스 인스턴스 */
    val weatherApiService: WeatherApiService by lazy {
        BackendRetrofitConfig.retrofit.create(WeatherApiService::class.java)
    }
    
    /** 사이드메뉴 즐겨찾기 지역 API 서비스 인스턴스 */
    val smFavoriteLocationApiService: SmFavoriteLocationApiService by lazy {
        BackendRetrofitConfig.retrofit.create(SmFavoriteLocationApiService::class.java)
    }
    
    // 향후 다른 백엔드 API 서비스 추가 시 여기에 추가
    // 예: finedustApiService, settingApiService 등
    
    // ===== 외부 API 서비스들 =====
    
    /** 카카오 로컬 API 서비스 인스턴스 */
    val kakaoApiService: KakaoApiService by lazy {
        ExternalRetrofitConfig.kakaoRetrofit.create(KakaoApiService::class.java)
    }
    
    // 향후 다른 외부 API 서비스 추가 시 여기에 추가
    // 예: naverApiService, googleApiService 등
}
