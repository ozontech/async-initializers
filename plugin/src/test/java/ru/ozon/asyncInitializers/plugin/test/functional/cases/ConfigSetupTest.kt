package ru.ozon.asyncInitializers.plugin.test.functional.cases

import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test
import ru.ozon.asyncInitializers.plugin.test.functional.framework.BaseGradleProjectTest
import java.io.File
import kotlin.test.assertTrue

/**
 * Tests of plugin setup correctness: extension wiring and buildType matching.
 */
internal class ConfigSetupTest : BaseGradleProjectTest() {

    override val templateDir: File = File("src/test/templates/android-app")

    @Test
    fun `validate task is created and executed with correct setup`() {
        val result = gradleRunner.runWithLog(":app:assembleDebug")

        assertTrue(
            result.tasks.any { it.path.contains("ClassesWithAsm") && it.outcome == TaskOutcome.SUCCESS },
            "The instrumentation task transform<Variant>ClassesWithAsm should complete successfully"
        )
        assertTrue(
            resultHasTaskWithOutcome(result, ":app:validateInitializersInjectDebug", TaskOutcome.SUCCESS),
            "Configuration is correct, but the :app:validateInitializersInjectDebug task was not successful"
        )
    }

    @Test
    fun `plugin is not applied to a container with a mismatched buildType`() {
        val result = gradleRunner.runWithLog(":app:assembleRelease")

        assertTrue(
            result.tasks.none { it.path.contains("ClassesWithAsm") && it.outcome == TaskOutcome.SUCCESS },
            "With the plugin disabled there should be no ClassesWithAsm instrumentation task"
        )
        assertTrue(
            result.tasks.none { it.path.contains("validateInitializersInject", ignoreCase = true) },
            "There should be no validate task for the debug variant, since config is set only for release"
        )
    }
}
