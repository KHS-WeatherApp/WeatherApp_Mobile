plugins {
    alias(libs.plugins.android.application)         // 안드로이드 애플리케이션 플러그인을 사용합니다.
    alias(libs.plugins.jetbrains.kotlin.android)    // 코틀린과 안드로이드를 함께 사용하기 위한 플러그인을 사용합니다.
    kotlin("kapt")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.example.kh_studyprojects_weatherapp" // 앱의 유니크한 네임스페이스를 설정합니다.
    compileSdk = 34 // 컴파일에 사용될 SDK 버전을 지정합니다.

    defaultConfig {
        applicationId = "com.example.kh_studyprojects_weatherapp" // 앱의 고유 ID를 설정합니다.
        minSdk = 30             // 앱이 지원하는 최소 안드로이드 버전을 지정합니다.
        targetSdk = 34          // 앱이 타겟으로 하는 안드로이드 SDK 버전을 지정합니다.
        versionCode = 1         // 앱 버전 코드, 앱 업데이트 시 변경되어야 합니다.
        versionName = "1.0.0"   // 앱 버전 이름, 사용자에게 보여지는 앱 버전입니다.

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner" // 테스트를 실행할 때 사용할 러너를 지정합니다.
    }

    buildTypes {
        release {
            isMinifyEnabled = false // 코드 난독화 여부를 설정합니다. false는 난독화하지 않음을 의미합니다.
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            ) // Proguard를 사용하여 코드 보호 규칙 파일을 지정합니다.
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17 // 소스 코드의 자바 호환성 버전을 지정합니다.
        targetCompatibility = JavaVersion.VERSION_17 // 타겟 자바 호환성 버전을 지정합니다.
    }
    kotlinOptions {
        jvmTarget = "17" // 코틀린 코드의 JVM 타겟 버전을 지정합니다.
    }
    buildFeatures {
        viewBinding = true // 뷰 바인딩을 활성화합니다. XML 레이아웃과 코드를 더 쉽게 연결할 수 있습니다.
        dataBinding = true // 데이터 바인딩을 활성화하여 뷰와 모델 데이터 간의 연결을 도와줍니다.
    }
}

dependencies {
    // 안드로이드 기본 의존성
    implementation(libs.androidx.core.ktx)              // 코틀린 안드로이드 확장 기능
    implementation(libs.androidx.appcompat)             // 하위 버전 호환성 지원
    implementation(libs.androidx.activity)              // 액티비티 관련 기능
    implementation(libs.androidx.constraintlayout)      // 제약 레이아웃
    implementation(libs.material)                       // 머티리얼 디자인 컴포넌트

    // 네비게이션 컴포넌트
    implementation(libs.androidx.navigation.fragment.ktx)  // 네비게이션 프래그먼트 기능
    implementation(libs.androidx.navigation.ui.ktx)        // 네비게이션 UI 컴포넌트

    // 네트워크 통신
    // Retrofit
    implementation(libs.retrofit)                       // REST API 통신 라이브러리
    implementation(libs.retrofit.gson.converter)        // JSON 변환기
    implementation(libs.gson)                           // JSON 파싱 라이브러리

    // OkHttp
    implementation(libs.okhttp)                        // HTTP 클라이언트
    implementation(libs.okhttp.logging)                // HTTP 요청/응답 로깅

    // 테스트
    // 단위 테스트
    testImplementation(libs.junit)                     // 단위 테스트 프레임워크

    // 안드로이드 테스트
    androidTestImplementation(libs.androidx.junit)      // 안드로이드 테스트 지원
    androidTestImplementation(libs.androidx.espresso.core) // UI 테스트 프레임워크

    // Hilt
    implementation("com.google.dagger:hilt-android:2.48.1")
    kapt("com.google.dagger:hilt-android-compiler:2.48.1")
    implementation("androidx.hilt:hilt-navigation-fragment:1.1.0")

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    
    // 구글 위치 정보 API
    implementation("com.google.android.gms:play-services-location:21.1.0")
}

kapt {
    correctErrorTypes = true
}
