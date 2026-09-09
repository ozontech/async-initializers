package ru.ozon.asyncInitializers.plugin.test.functional.framework

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.io.TempDir
import java.io.File

/**
 * Base class for functional tests of the component-initializer-injector plugin.
 *
 * Copies [templateDir] (an Android application) into a temporary directory, includes
 * test.settings.gradle.kts and creates gradle.properties with the plugin enabled by default.
 */
internal abstract class BaseGradleProjectTest {

    /** Path to the template directory, e.g. "src/test/templates/android-app" */
    abstract val templateDir: File

    @field:TempDir
    lateinit var testProject: File

    lateinit var gradleRunner: GradleRunner

    @BeforeEach
    fun setup() {
        val testSettings = File(templateDir.parentFile, "test.settings.gradle.kts")
        templateDir.copyRecursively(testProject, true)
        testSettings.copyTo(File(testProject, "test.settings.gradle.kts"))

        val settings = File(testProject, "settings.gradle.kts")
        val gradleProperties = File(testProject, "gradle.properties")

        if (!settings.exists()) {
            settings.createNewFile()
        }
        settings.appendText("\napply(\"test.settings.gradle.kts\")")

        if (!gradleProperties.exists()) {
            gradleProperties.createNewFile()
            gradleProperties.writeText(
                """
                    |android.useAndroidX=true
                """.trimMargin()
            )
        }

        gradleRunner = GradleRunner.create()
            .withPluginClasspath()
            .withProjectDir(testProject)
    }

    protected fun GradleRunner.runWithLog(vararg args: String): BuildResult {
        withArguments(
            *args,
            "--console=verbose",
            "--stacktrace",
        )
        val result = build()
        println(result.output)

        return result
    }

    protected fun GradleRunner.runAndFailWithLog(vararg args: String): BuildResult {
        withArguments(
            *args,
            "--console=verbose",
            "--stacktrace",
        )
        val result = buildAndFail()

        return result
    }

    // region Template files that can be overridden in tests

    protected fun configFile(): File = File(testProject, "app/inject.config")

    protected fun setConfigContent(content: String) {
        configFile().writeText(content)
    }

    // endregion

    /** Patched victim class — result of the transform<Variant>ClassesWithAsm task */
    protected val patchedComponentFile: File
        get() = File(
            testProject,
            "app/build/intermediates/classes/debug/transformDebugClassesWithAsm/dirs/ru/ozon/test/ComponentA.class"
        )

    // region Result assertions

    protected fun resultHasTaskWithOutcome(result: BuildResult, path: String, outcome: TaskOutcome): Boolean {
        return result.tasks.any { it.path == path && it.outcome == outcome }
    }

    // endregion
}
