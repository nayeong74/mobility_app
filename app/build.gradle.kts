import org.jetbrains.kotlin.storage.CacheResetOnProcessCanceled.enabled
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

val properties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    properties.load(localPropertiesFile.inputStream())
}

android {
    namespace = "com.example.myapplication"
    compileSdk = 35

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 30
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
//        buildConfigField("String", "KAKAO_MAP_KEY", properties.getProperty("KAKAO_MAP_KEY"))
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    // Android 기본 라이브러리
    implementation(libs.androidx.core.ktx)
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation(libs.material)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.play.services.maps)

    // Glide (이미지 로딩)
    implementation(libs.glide)
    implementation(libs.play.services.location)
    annotationProcessor(libs.compiler)

    // Retrofit (HTTP 요청)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.converter.scalars)


    // Gson (JSON 파싱)
    implementation(libs.gson)

    // 테스트 라이브러리
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit.v115)
    androidTestImplementation(libs.androidx.espresso.core)

    // 네트워크 라이브러리
    implementation(libs.okhttp)


    // fragment
    implementation (libs.androidx.fragment.ktx)
    implementation (libs.androidx.lifecycle.viewmodel.ktx)
    implementation (libs.androidx.lifecycle.livedata.ktx)


//    // Mapbox
//    implementation ("com.mapbox.mapboxsdk:mapbox-android-sdk:11.8.1")

    implementation(libs.play.services.maps) // Google Maps
    implementation ("com.google.android.gms:play-services-location:21.0.1")

    implementation(libs.naver.map.sdk) // Naver Maps
//    implementation(libs.kakao.sdk) // Kakao API

//    // Speech-to-Text API
//    implementation ("com.google.cloud:google-cloud-speech:2.3.0")
//
//    // Text-to-Speech API
//    implementation ("com.google.cloud:google-cloud-texttospeech:2.3.0")


}

