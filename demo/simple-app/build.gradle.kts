plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    id("ru.ozon.async-initializer-injector")
}

injectInitializerComponents {
    create("debug") {
        configs = listOf(layout.projectDirectory.file("demoComponentInitializerConfig.config"))
        isTransformResultEnabled = true
    }
}

android {
    namespace = "ru.ozon.asyncInitializer.demo.plugin"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "ru.ozon.asyncInitializer.demo.plugin"
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
    implementation(libs.appCompatDemo)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.foundation)
    implementation(libs.compose.material)
    implementation(libs.compose.material.icons)
    implementation(libs.compose.runtime)
    implementation(libs.compose.ui)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.coreKtx)
    implementation(libs.kotlinx.collections.immutable)
}