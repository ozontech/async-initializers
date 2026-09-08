package ru.ozon.asyncInitializers.plugin.test.functional.cases

import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test
import ru.ozon.asyncInitializers.plugin.test.functional.framework.BaseGradleProjectTest
import java.io.File
import kotlin.test.assertTrue

/**
 * Тесты применения патчинга байт-кода: в публичные методы класса-жертвы
 * встраивается вызов инициалайзера.
 *
 * Патчинг выполняется задачей AGP-инструментирования `transform<Variant>ClassesWithAsm`,
 * которую плагин регистрирует через `variant.instrumentation.transformClassesWith(...)`.
 * Результат лежит в `app/build/intermediates/classes/<variant>/transform<Variant>ClassesWithAsm/dirs/...`.
 */
internal class PatchingAppliedTest : BaseGradleProjectTest() {

    override val templateDir: File = File("src/test/templates/android-app")

    @Test
    fun `патчинг применяется через задачу инструментирования ClassesWithAsm`() {
        val result = gradleRunner.runWithLog(":app:assembleDebug")

        assertTrue(
            result.tasks.any { it.path.contains("ClassesWithAsm") && it.outcome == TaskOutcome.SUCCESS },
            "Задача инструментирования transform<Variant>ClassesWithAsm должна выполниться успешно"
        )
    }

    @Test
    fun `в байткод класса-жертвы встраивается вызов инициалайзера`() {
        gradleRunner.runWithLog(":app:assembleDebug")

        assertTrue(
            patchedComponentFile.exists(),
            "Пропатченный класс-жертва не найден по пути $patchedComponentFile"
        )

        patchedComponentFile.readText(Charsets.ISO_8859_1).let { bytes ->
            assertTrue(
                bytes.contains("WasInjectInitializer"),
                "В классе-жертве ${patchedComponentFile.name} должен присутствовать маркер WasInjectInitializer"
            )
            assertTrue(
                bytes.contains("getComponentInitializer"),
                "В классе-жертве ${patchedComponentFile.name} должен присутствовать вызов getComponentInitializer"
            )
        }
    }
}