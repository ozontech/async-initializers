package ru.ozon.asyncInitializers.plugin.internal.util

import org.gradle.api.Project
import org.gradle.api.file.RegularFile

internal fun Project.rootRegularFile(fileName: String): RegularFile {
    return rootProject.layout.projectDirectory.file(fileName)
}
