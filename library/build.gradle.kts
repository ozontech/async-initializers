plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "ru.ozon.asyncInitializer.library"
    compileSdk = libs.versions.compileSdk.get().toInt()


    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(libs.coreKtx)
    testImplementation(libs.kotlinJunit)
}