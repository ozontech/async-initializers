package ru.ozon.asyncInitializers.plugin.test.functional.cases

import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test
import ru.ozon.asyncInitializers.plugin.test.functional.framework.BaseGradleProjectTest
import java.io.File
import kotlin.test.assertTrue

/**
 * Tests of bytecode patching: into the public methods of the victim class
 * an initializer call is embedded.
 *
 * Patching is done by the AGP instrumentation task `transform<Variant>ClassesWithAsm`,
 * which the plugin registers via `variant.instrumentation.transformClassesWith(...)`.
 * The result is located in `app/build/intermediates/classes/<variant>/transform<Variant>ClassesWithAsm/dirs/...`.
 */
internal class PatchingAppliedTest : BaseGradleProjectTest() {

    override val templateDir: File = File("src/test/templates/android-app")

    @Test
    fun `patching is applied through the ClassesWithAsm instrumentation task`() {
        val result = gradleRunner.runWithLog(":app:assembleDebug")

        assertTrue(
            result.tasks.any { it.path.contains("ClassesWithAsm") && it.outcome == TaskOutcome.SUCCESS },
            "The instrumentation task transform<Variant>ClassesWithAsm should complete successfully"
        )
    }

    @Test
    fun `an initializer call is embedded into the victim class bytecode`() {
        gradleRunner.runWithLog(":app:assembleDebug")

        assertTrue(
            patchedComponentFile.exists(),
            "Patched victim class not found at path $patchedComponentFile"
        )

        patchedComponentFile.readText(Charsets.ISO_8859_1).let { bytes ->
            assertTrue(
                bytes.contains("WasInjectInitializer"),
                "Victim class ${patchedComponentFile.name} should contain the WasInjectInitializer marker"
            )
            assertTrue(
                bytes.contains("getComponentInitializer"),
                "Victim class ${patchedComponentFile.name} should contain a getComponentInitializer call"
            )
        }
    }
}