package ru.ozon.asyncInitializers.plugin.test.functional.cases

import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test
import ru.ozon.asyncInitializers.plugin.test.functional.framework.BaseGradleProjectTest
import java.io.File
import kotlin.test.assertTrue

/**
 * Тесты корректности настройки плагина: подключение расширения и сопоставление buildType.
 */
internal class ConfigSetupTest : BaseGradleProjectTest() {

    override val templateDir: File = File("src/test/templates/android-app")

    @Test
    fun `при корректной настройке создаётся и выполняется validate задача`() {
        val result = gradleRunner.runWithLog(":app:assembleDebug")

        assertTrue(
            result.tasks.any { it.path.contains("ClassesWithAsm") && it.outcome == TaskOutcome.SUCCESS },
            "Задача инструментирования transform<Variant>ClassesWithAsm должна выполниться успешно"
        )
        assertTrue(
            resultHasTaskWithOutcome(result, ":app:validateInitializersInjectDebug", TaskOutcome.SUCCESS),
            "Конфигурация корректна, но задача :app:validateInitializersInjectDebug не выполнилась успешно"
        )
    }

    @Test
    fun `плагин не применяется к контейнеру с несовпадающим buildType`() {
        val result = gradleRunner.runWithLog(":app:assembleRelease")

        assertTrue(
            result.tasks.none { it.path.contains("ClassesWithAsm") && it.outcome == TaskOutcome.SUCCESS },
            "При отключённом плагине не должно быть задачи инструментирования ClassesWithAsm"
        )
        assertTrue(
            result.tasks.none { it.path.contains("validateInitializersInject", ignoreCase = true) },
            "Для дебаг-варианта не должно быть validate задачи, т.к. конфиг задан только для release"
        )
    }
}
