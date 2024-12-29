// 안드로이드 앱 개발에 필요한 플러그인 설정
plugins {
    alias(libs.plugins.androidApplication)      // 안드로이드 애플리케이션 플러그인
    alias(libs.plugins.jetbrainsKotlinAndroid) // 코틀린 안드로이드 플러그인
}

android {
    // 앱의 기본 설정
    namespace = "com.example.kh_studyprojects_weatherapp" // 앱의 고유 패키지명
    compileSdk = 34 // 컴파일에 사용할 안드로이드 SDK 버전

    defaultConfig {
        applicationId = "com.example.kh_studyprojects_weatherapp" // Google Play에서 앱을 식별하는 고유 ID
        minSdk = 26    // 앱이 지원하는 최소 안드로이드 버전
        targetSdk = 34 // 앱이 목표로 하는 안드로이드 버전
        versionCode = 1 // 앱의 버전 코드 (업데이트마다 증가)
        versionName = "1.0" // 사용자에게 보여지는 앱 버전

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner" // 안드로이드 테스트 실행기
    }

    // 빌드 타입 설정
    buildTypes {
        release {
            isMinifyEnabled = false // 코드 축소 및 난독화 비활성화
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), // 기본 ProGuard 규칙
                "proguard-rules.pro" // 사용자 정의 ProGuard 규칙
            )
        }
    }

    // 자바 컴파일 옵션
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8 // 소스 코드의 자바 버전
        targetCompatibility = JavaVersion.VERSION_1_8 // 타겟 자바 버전
    }

    // 코틀린 컴파일 옵션
    kotlinOptions {
        jvmTarget = "1.8" // 코틀린 컴파일 타겟 JVM 버전
    }

    // 빌드 기능 설정
    buildFeatures {
        buildConfig = true // BuildConfig 클래스 생성 활성화
    }
}

// 프로젝트 의존성 설정
dependencies {
    // AndroidX 핵심 라이브러리
    implementation(libs.androidx.core.ktx)              // 코틀린 안드로이드 확장 기능
    implementation(libs.androidx.appcompat)             // 하위 버전 호환성 지원
    implementation(libs.material)                       // 머티리얼 디자인 컴포넌트
    implementation(libs.androidx.activity)              // 액티비티 관련 기능
    implementation(libs.androidx.constraintlayout)      // 제약 레이아웃
    implementation(libs.androidx.navigation.fragment.ktx) // 네비게이션 프래그먼트
    implementation(libs.androidx.navigation.ui.ktx)     // 네비게이션 UI 컴포넌트

    // Retrofit 네트워킹 라이브러리
    implementation(libs.retrofit)                       // Retrofit 핵심 라이브러리
    implementation(libs.gson)                           // JSON 파싱을 위한 Gson
    implementation(libs.retrofit.gson.converter)        // Retrofit Gson 컨버터

    // 테스트 관련 의존성
    testImplementation(libs.junit)                      // 단위 테스트
    androidTestImplementation(libs.androidx.junit)      // 안드로이드 테스트
    androidTestImplementation(libs.androidx.espresso.core) // UI 테스트

    // OkHttp 관련 의존성
    implementation(libs.okhttp)                         // OkHttp 클라이언트
    implementation(libs.loggingInterceptor)            // OkHttp 로깅 인터셉터
}