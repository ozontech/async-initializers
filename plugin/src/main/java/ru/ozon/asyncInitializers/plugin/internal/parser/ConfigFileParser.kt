package ru.ozon.asyncInitializers.plugin.internal.parser

import org.gradle.api.file.RegularFile
import ru.ozon.asyncInitializers.plugin.internal.image.images.ModifiedImage
import ru.ozon.asyncInitializers.plugin.internal.parser.lineParsers.ModifiedLineParser
import ru.ozon.asyncInitializers.plugin.internal.patchingController.ProgramPatchingController
import ru.ozon.asyncInitializers.plugin.internal.reader.lineReadFile
import java.io.File

internal object ConfigFileParser {

    fun createProgramPatchingController(
        modifiedImages: List<ModifiedImage>
    ): ProgramPatchingController {
        val builder = ProgramPatchingController.Builder()
        modifiedImages.forEach(builder::addType)

        return builder.build()
    }

    fun readModifiedTypes(
        files: List<RegularFile>
    ): List<ModifiedImage> = readModifiedTypes(files.map { it.asFile })

    @JvmName("readModifiedTypesFromSimpleFiles")
    fun readModifiedTypes(
        files: List<File>
    ): List<ModifiedImage> = buildList {
        val modifiedLineParser = ModifiedLineParser()

        files.forEach { file ->
            file.lineReadFile {
                var line = currentLine
                while (line != null) {
                    modifiedLineParser.createImageOrNull(this)?.let(::add)
                    line = nextLine()
                }
            }
        }
    }
}

