plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "ru.ozon.asyncInitializer.demo.app"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "ru.ozon.asyncInitializer.demo.app"
        minSdk = libs.versions.minSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":library"))
//    implementation("ru.ozon:asyncInitializers:1.0.0")

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.foundation)
    implementation(libs.compose.material)
    implementation(libs.compose.material.icons)
    implementation(libs.compose.runtime)
    implementation(libs.compose.ui)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.appCompatDemo)
    implementation(libs.androidx.ui.tooling.preview)

    implementation(libs.coreKtx)

    implementation(libs.kotlinx.serialization.json)
}