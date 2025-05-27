plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.devtoolsKsp)
    id("kotlin-kapt") // Исправлено: правильный способ подключения kapt
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.vkr_healthy_nutrition"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.vkr_healthy_nutrition"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    // Базовые зависимости Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.preference)

    // Room dependencies
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.runtime.livedata)

    // Networking dependencies
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.3") // Optional: for logging network requests

    // Charts
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // Обработчики Room
    kapt(libs.androidx.room.compiler)  // Используем kapt с плагином kotlin-kapt

    // Тестирование
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Mockito for local tests
    testImplementation("org.mockito:mockito-core:5.8.0") // Используйте актуальную версию
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.0.0") // Используйте актуальную версию

    // Robolectric for local tests
    testImplementation("org.robolectric:robolectric:4.9") // Используйте актуальную версию

    // Coroutines test
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0") // Используйте актуальную версию

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.7.2"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx") // Firestore может быть полезен для хранения данных пользователя
}