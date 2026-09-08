plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("ru.ozon.gradle.plugin.asyncInitializer-injector")
}

injectInitializerComponents {
    create("debug") {
        configs = listOf(layout.projectDirectory.file("inject.config"))
        isTransformResultEnabled = true
    }
}

android {
    namespace = "ru.ozon.test"
    compileSdk = 34

    defaultConfig {
        applicationId = "ru.ozon.test"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}
