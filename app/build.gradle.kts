import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.example.echo_panda_mobile"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.echo_panda_mobile"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Production API. Override in debug for local backend (emulator → host machine).
        buildConfigField("String", "API_BASE_URL", "\"https://api.echopanda.me/\"")
    }

    buildTypes {
        debug {
            // Same production API as release (web + mobile both use api.echopanda.me).
            buildConfigField("String", "API_BASE_URL", "\"https://api.echopanda.me/\"")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

// Dependencies
dependencies {
    // Navigation Compose
    implementation(libs.androidx.navigation.compose)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.messaging)

    // Lifecycle
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    
    // DataStore Preferences
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    
    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging)
    implementation(libs.androidx.security.crypto)

    // Image Loading
    implementation(libs.coil.compose)
    
    // Audio/Media
    implementation("androidx.media3:media3-exoplayer:1.1.1")
    implementation("androidx.media3:media3-ui:1.1.1")
    implementation("androidx.media3:media3-common:1.1.1")
    
    // Google Sign-In
    implementation(libs.google.play.services.auth)
    
    implementation(libs.androidx.material.icons)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    
    // Material Components (required for XML themes)
    implementation(libs.material)

    // Media3 ExoPlayer
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.session)
    implementation(libs.androidx.media3.common)
    implementation(libs.androidx.media3.datasource.okhttp)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}
