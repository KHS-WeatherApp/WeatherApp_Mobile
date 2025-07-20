package com.example.kh_studyprojects_weatherapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * 날씨 앱의 Application 클래스
 * 
 * 🚨 주의: 이 클래스는 절대 삭제하면 안됩니다! 🚨
 * 
 * 역할:
 * 1. Dagger Hilt 의존성 주입 프레임워크의 진입점
 * 2. 앱 전체에서 @AndroidEntryPoint, @HiltViewModel 등이 작동하도록 함
 * 3. MainActivity, Fragment들, ViewModel들의 의존성 주입 활성화
 * 
 * 사용되는 곳:
 * - AndroidManifest.xml에서 android:name=".WeatherApplication"로 등록
 * - MainActivity (@AndroidEntryPoint)
 * - 모든 Fragment들 (@AndroidEntryPoint)  
 * - ViewModel들 (@HiltViewModel)
 * - AppModule.kt, RepositoryModule.kt (@Module)
 * 
 * 삭제하면 발생하는 문제:
 * - 컴파일 에러 발생
 * - 의존성 주입 실패로 앱 크래시
 * - WeatherRepository, LocationManager 등 주입 불가
 * 
 * @author 김지윤
 * @since 2025.05.18
 * @version 1.0
 */
@HiltAndroidApp
class WeatherApplication : Application() 