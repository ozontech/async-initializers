plugins {
    alias(libs.plugins.android.application)
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

}

dependencies {

}