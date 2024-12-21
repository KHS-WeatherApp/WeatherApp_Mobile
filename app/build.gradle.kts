plugins {
    alias(libs.plugins.android.application)         // 안드로이드 애플리케이션 플러그인을 사용합니다.
    alias(libs.plugins.jetbrains.kotlin.android)    // 코틀린과 안드로이드를 함께 사용하기 위한 플러그인을 사용합니다.
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
        sourceCompatibility = JavaVersion.VERSION_1_8 // 소스 코드의 자바 호환성 버전을 지정합니다.
        targetCompatibility = JavaVersion.VERSION_1_8 // 타겟 자바 호환성 버전을 지정합니다.
    }
    kotlinOptions {
        jvmTarget = "1.8" // 코틀린 코드의 JVM 타겟 버전을 지정합니다.
    }
    buildFeatures {
        viewBinding = true // 뷰 바인딩을 활성화합니다. XML 레이아웃과 코드를 더 쉽게 연결할 수 있습니다.
        dataBinding = true // 데이터 바인딩을 활성화하여 뷰와 모델 데이터 간의 연결을 도와줍니다.
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)                  // 안드로이드 KTX 코어 라이브러리를 포함합니다.
    implementation(libs.androidx.appcompat)                 // 앱 호환성을 위한 AppCompat 라이브러리를 포함합니다.
    implementation(libs.material)                           // 구글의 Material 디자인 컴포넌트를 포함합니다.
    implementation(libs.androidx.activity)                  // 안드로이드 액티비티 관련 라이브러리를 포함합니다.
    implementation(libs.androidx.constraintlayout)          // 레이아웃 제약 조건을 위한 라이브러리를 포함합니다.
    implementation(libs.androidx.navigation.fragment.ktx)   // 네비게이션 프래그먼트를 위한 라이브러리를 포함합니다.
    implementation(libs.androidx.navigation.ui.ktx)         // 레이아웃 제약 조건을 위한 라이브러리를 포함합니다.

    // Retrofit
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.core)
    implementation(libs.okhttp.logging)

    testImplementation(libs.junit)                          // JUnit 테스트 프레임워크를 포함합니다.
    androidTestImplementation(libs.androidx.junit)          // 안드로이드 JUnit 테스트를 위한 라이브러리를 포함합니다.
    androidTestImplementation(libs.androidx.espresso.core)  // UI 테스트를 위한 Espresso 코어 라이브러리를 포함합니다.

}
