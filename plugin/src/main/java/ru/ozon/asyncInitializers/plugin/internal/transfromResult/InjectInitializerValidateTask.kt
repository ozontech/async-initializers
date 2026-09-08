package ru.ozon.asyncInitializers.plugin.internal.transfromResult

import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import ru.ozon.asyncInitializers.plugin.internal.parser.ConfigFileParser
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream

/**
 * Task, суть которой заключается в проверке, правильно ли произошел патчинг байт-кода
 *
 * В текущей реализации берет jar всех библиотек и classes из текущего проекта
 */
internal abstract class InjectInitializerValidateTask : DefaultTask() {

    @get:InputFiles
    abstract val configs: ListProperty<RegularFile>

    @get:InputFiles
    abstract val allJars: ListProperty<RegularFile>

    @get:InputFiles
    abstract val allDirectories: ListProperty<Directory>

    @get:OutputFile
    abstract val output: RegularFileProperty

    @Internal
    val jarPaths = mutableSetOf<String>()

    @TaskAction
    fun action() {
        val modifiedTypes = ConfigFileParser.readModifiedTypes(configs.get())
        val builder = PatchingResult.Builder(modifiedTypes)

        outputStream { jarOutput ->
            allJars.get().forEach { file ->
                JarFile(file.asFile, false).use { jarFile ->
                    jarFile.entries().iterator().forEach { jarEntry ->
                        jarOutput.writeEntity(jarEntry.name, jarFile.getInputStream(jarEntry), builder)
                    }
                }
            }

            allDirectories.get().forEach { directory ->
                directory.asFile.walk().forEach { file ->
                    if (file.isFile) {
                        val relativePath = directory.asFile.toURI().relativize(file.toURI()).getPath()
                        jarOutput.writeEntity(relativePath.replace(File.separatorChar, '/'), file.inputStream(), builder)
                    }
                }
            }
        }

        builder.build().checkOnValidTransformation()
    }

    private fun ByteArray.validate(builder: PatchingResult.Builder) {
        val classReader = ClassReader(this)
        val classWriter = ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
        val classVisitor = InjectInitializerValidateClassVisitor(Opcodes.ASM9, classWriter, builder)
        classReader.accept(classVisitor, 0)
    }

    private fun JarOutputStream.writeEntity(
        name: String,
        inputStream: InputStream,
        builder: PatchingResult.Builder
    ) {
        if (!jarPaths.contains(name)) {
            val bytes = inputStream.readBytes()
            if (name.endsWith(".class", ignoreCase = true) && builder.isNeedValidate(name)) {
                bytes.validate(builder)
            }
            putNextEntry(JarEntry(name))
            write(bytes)
            closeEntry()
            jarPaths.add(name)
        }
    }

    private fun outputStream(block: (JarOutputStream) -> Unit) {
        val fileOutputStream = FileOutputStream(output.get().asFile)
        val bufferedOutputStream = BufferedOutputStream(fileOutputStream)
        JarOutputStream(bufferedOutputStream).use(block)
    }

}
