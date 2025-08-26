package com.example.kh_studyprojects_weatherapp.data.api

import com.example.kh_studyprojects_weatherapp.data.api.weather.WeatherApiService
import com.example.kh_studyprojects_weatherapp.data.api.sidemenu.SmFavoriteLocationApiService

/**
 * 백엔드 API 서비스 제공자
 * 
 * 자체 백엔드 서버의 모든 API 서비스 인스턴스들을 제공합니다.
 * BackendRetrofitInstance의 retrofit을 사용하여 각 API 서비스를 생성합니다.
 *
 * @author 김효동
 * @since 2025.08.26
 * @version 1.0
 */
object BackendApiServiceProvider {
    
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
}
