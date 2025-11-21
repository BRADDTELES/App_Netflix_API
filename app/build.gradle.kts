import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.danilloteles.appnetflixapi"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        val properties = Properties()
        val localPropertiesFile = project.rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            properties.load(localPropertiesFile.inputStream())
        }
        val apiKey = properties.getProperty("API_KEY") ?: ""
        val apiReadAccessToken = properties.getProperty("API_READ_ACCESS_TOKEN") ?: ""
        buildConfigField("String", "API_KEY", apiKey)
        buildConfigField("String", "API_READ_ACCESS_TOKEN", apiReadAccessToken)

        applicationId = "com.danilloteles.appnetflixapi"
        minSdk = 24
        targetSdk = 36
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
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    // Paging 3 para o core (runtime)
    implementation(libs.androidx.paging.runtime)
    // Paging 3para integração com Jetpack Compose
    implementation(libs.androidx.paging.compose)
    // Lifecycle Compose
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    // Retrofit e Gson
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    // Navigation Compose
    implementation(libs.androidx.navigation.compose)
    // Splash Screen API
    implementation(libs.androidx.core.splashscreen)
    // Coil para imagens - VERSÃO 3.x (Compose Multiplatform)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    // Icons Extended - versão gerenciada pelo BOM
    implementation(libs.androidx.compose.material.icons.extended)
    // Material3 - versão gerenciada pelo BOM
    implementation(libs.androidx.material3)
    implementation(libs.material3)
    // DataStore
    implementation(libs.androidx.datastore.preferences)
    // Browser
    implementation(libs.androidx.browser)
    // Room
    implementation(libs.androidx.room.runtime)
    // Add the KSP plugin to your project
    ksp(libs.androidx.room.compiler)
    // Kotlin Extensions and Coroutines support for Room
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.compose.material3.v140)
    implementation(libs.androidx.compose.material3.window.size.class1)
    implementation(libs.androidx.compose.material3.adaptive.navigation.suite)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}