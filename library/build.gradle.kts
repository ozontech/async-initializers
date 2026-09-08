import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.vanniktech.maven.publish)
}

val uploadInfo = Properties().apply {
    file("versions.properties").inputStream().use { load(it) }
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

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    coordinates(
        uploadInfo.getProperty("groupId"),
        uploadInfo.getProperty("artifactId"),
        uploadInfo.getProperty("versionName")
    )

    pom {
        name.set("AsyncInitializers")
        description.set("A library for safe and thread-safe initialization of application components. Provides a framework for managing initialization dependencies to speed up Android app startup through asynchronous initialization.")
        url.set("https://github.com/ozontech/async-initializers")

        scm {
            url.set("https://github.com/ozontech/async-initializers")
            connection.set("scm:git:git://git@github.com:ozontech/async-initializers.git")
            developerConnection.set("scm:git:ssh://git@github.com:ozontech/async-initializers.git")
        }

        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }

        developers {
            developer {
                id.set("Privatik")
                name.set("Ivan Kurak")
                url.set("https://github.com/Privatik/")
            }
            developer {
                id.set("offthelightness")
                name.set("Dmitriy Glagolev")
                url.set("https://github.com/offthelightness/")
            }
        }

        organization {
            name = "Ozon Tech"
            url = "https://github.com/ozontech/"
        }
    }
}