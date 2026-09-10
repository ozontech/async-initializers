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

group = uploadInfo.getProperty("groupId")
version = uploadInfo.getProperty("versionName")

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

// AGP and the Kotlin plugin are declared compileOnly and do not end up in the TestKit build.
// We inject them into the plugin classpath so that in the test Android project
// applications of com.android.application and kotlin-android resolve.
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

    // AGP and the Kotlin plugin go into the TestKit build via the plugin classpath (compileOnly is not included)
    "fixtureClasspath"(libs.androidPlugin)
    "fixtureClasspath"(libs.kotlinPlugin)
}

gradlePlugin {
    website.set("https://github.com/ozontech/async-initializers")
    vcsUrl.set("https://github.com/ozontech/async-initializers")
    plugins {
        create("asyncInitializers-injector") {
            id = uploadInfo.getProperty("pluginId")
            displayName = "AsyncInitializer Injector"
            description = "Gradle plugin that injects AsyncInitializer calls into public methods of declared classes at build time."
            tags = listOf("android")
            implementationClass = "ru.ozon.asyncInitializers.plugin.InjectInitializerPlugin"
        }
    }
}
