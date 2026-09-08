import java.util.Properties
import kotlin.apply

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    alias(libs.plugins.gradle.publish)
}

val uploadInfo = Properties().apply {
    file("versions.properties").inputStream().use { load(it) }
}

kotlin {
    jvmToolchain(17)
    explicitApi()
}

java {
    withJavadocJar()
    withSourcesJar()
}


tasks.test {
    useJUnitPlatform()
}

// AGP и Kotlin-плагин объявлены compileOnly и не попадают в TestKit-сборку.
// Инжектируем их в plugin classpath плагина, чтобы в тестовом Android-проекте
// резолвились применения com.android.application и kotlin-android.
val fixtureClasspath: Configuration by configurations.creating

tasks.withType<PluginUnderTestMetadata> {
    pluginClasspath.from(fixtureClasspath)
}


dependencies {
    compileOnly(libs.androidPlugin)
    compileOnly(libs.kotlinPlugin)

    compileOnly(libs.asm)
    compileOnly(libs.asm.tree)
    compileOnly(libs.asm.commons)


    testImplementation(gradleTestKit())
    testImplementation(kotlin("test"))
    testImplementation(libs.jupiter)
    testImplementation(libs.assertj)
    testImplementation(libs.asm)
    testImplementation(libs.asm.tree)
    testImplementation(libs.asm.commons)
    testImplementation(libs.androidPlugin)

    // AGP и Kotlin-плагин уходят в TestKit-сборку через plugin classpath (compileOnly не попадает)
    "fixtureClasspath"(libs.androidPlugin)
    "fixtureClasspath"(libs.kotlinPlugin)
}

gradlePlugin {
    website.set("https://github.com/ozontech/async-initializers")
    vcsUrl.set("https://github.com/ozontech/async-initializers")
    plugins {
        create("component-initializer-injector") {
            id = uploadInfo.getProperty("pluginId")
            displayName = "AsyncInitializer Injector"
            description = ""
            tags = listOf("android")
            implementationClass = "ru.ozon.asyncInitializers.plugin.InjectInitializerPlugin"
        }
    }
}
