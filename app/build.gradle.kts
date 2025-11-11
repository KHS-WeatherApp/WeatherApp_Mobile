import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.hilt.android)
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}

android {
    namespace = "com.example.kh_studyprojects_weatherapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.kh_studyprojects_weatherapp"
        minSdk = 30
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "KAKAO_API_KEY", "\"${localProperties.getProperty("KAKAO_API_KEY", "")}\"")
    }

    buildTypes {
        debug {
            buildConfigField("String", "BACKEND_BASE_URL", "\"http://192.168.10.84:8080\"") // 김효동 - 회사
//            buildConfigField("String", "BACKEND_BASE_URL", "\"http://172.25.192.1:8080\"") // 김효동 -
//            buildConfigField("String", "BACKEND_BASE_URL", "\"http://172.30.1.53:8080\"") // 김지윤
            buildConfigField("String", "BACKEND_EMULATOR_BASE_URL", "\"http://10.0.2.2:8080\"") // 에뮬레이터에서 접속용 서버 주소
            buildConfigField("Boolean", "ENABLE_BACKEND_HTTP_LOGGING", "true")
            buildConfigField("Boolean", "ENABLE_EXTERNAL_HTTP_LOGGING", "true")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // 운영 서버 주소면 그대로 두고, 운영 서버가 바뀌면 여기 수정
            buildConfigField("String", "BACKEND_BASE_URL", "\"https://api.example.com\"")
            buildConfigField("String", "BACKEND_EMULATOR_BASE_URL", "\"https://api.example.com\"")
            buildConfigField("Boolean", "ENABLE_BACKEND_HTTP_LOGGING", "false")
            buildConfigField("Boolean", "ENABLE_EXTERNAL_HTTP_LOGGING", "false")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        viewBinding = true
        dataBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.material)
    implementation(libs.androidx.recyclerview)

    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // Fragment 및 Lifecycle 명시적 버전 지정 (Navigation과 호환성 확보)
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

    implementation(libs.retrofit)
    implementation(libs.retrofit.gson.converter)
    implementation(libs.gson)

    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.androidx.swiperefreshlayout)

    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Hilt
    implementation("com.google.dagger:hilt-android:2.56")
    ksp("com.google.dagger:hilt-android-compiler:2.56")
    implementation("androidx.hilt:hilt-navigation-fragment:1.2.0")

    // Location
    implementation("com.google.android.gms:play-services-location:21.1.0")
}
