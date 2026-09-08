package ru.ozon.asyncInitializers.plugin.test.functional.cases

import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import ru.ozon.asyncInitializers.plugin.test.functional.framework.BaseGradleProjectTest
import java.io.File
import kotlin.test.assertTrue

/**
 * Тесты задачи validate (isTransformResultEnabled = true), которая вызывается после патчинга
 * и проверяет, что все заявленные в конфиге классы были найдены и пропатчены.
 */
internal class ValidateTaskTest : BaseGradleProjectTest() {

    override val templateDir: File = File("src/test/templates/android-app")

    private val validateTaskPath = ":app:validateInitializersInjectDebug"

    @Test
    fun `validate задача успешно проходит после корректного патчинга`() {
        val result = gradleRunner.runWithLog(":app:assembleDebug")

        assertTrue(
            resultHasTaskWithOutcome(result, validateTaskPath, TaskOutcome.SUCCESS),
            "После корректного патчинга $validateTaskPath должна выполниться успешно"
        )
    }

    @Test
    fun `validate задача падает если класс-жертва из конфига не найден`() {
        setConfigContent(
            "inject ru.ozon.test.InitializerA toPublicMethods ru.ozon.test.MissingComponent"
        )

        val result = gradleRunner.runAndFailWithLog(":app:assembleDebug")

        assertTrue(
            resultHasTaskWithOutcome(result, validateTaskPath, TaskOutcome.FAILED),
            "Задача $validateTaskPath должна упасть, если жертва не найдена"
        )
        assertTrue(
            result.output.contains("не был модифицирован", ignoreCase = true),
            "В выводе должно быть сообщение о ненайденном классе-жертве"
        )
    }

    @Test
    fun `validate задача падает если инициалайзер из конфига не найден`() {
        setConfigContent(
            "inject ru.ozon.test.MissingInitializer toPublicMethods ru.ozon.test.ComponentA"
        )

        val result = gradleRunner.runAndFailWithLog(":app:assembleDebug")

        assertTrue(
            resultHasTaskWithOutcome(result, validateTaskPath, TaskOutcome.FAILED),
            "Задача $validateTaskPath должна упасть, если инициалайзер не найден"
        )
        assertTrue(
            result.output.contains("не был найден", ignoreCase = true),
            "В выводе должно быть сообщение о ненайденном инициалайзере"
        )
    }

    @Test
    fun `validate задача падает если в ignore указан несуществующий метод`() {
        setConfigContent(
            """
                inject ru.ozon.test.InitializerA toPublicMethods ru.ozon.test.ComponentA {
                    ignore fun missingMethod()
                }
            """.trimIndent()
        )

        val result = gradleRunner.runAndFailWithLog(":app:assembleDebug")

        assertTrue(
            resultHasTaskWithOutcome(result, validateTaskPath, TaskOutcome.FAILED),
            "Задача $validateTaskPath должна упасть, если в ignore указан несуществующий метод"
        )
        assertTrue(
            result.output.contains("найти метод", ignoreCase = true),
            "В выводе должно быть сообщение о ненайденном ignore-методе"
        )
    }

    @Test
    fun `ignore исключает указанный метод из патчинга`() {
        setConfigContent(
            """
                inject ru.ozon.test.InitializerA toPublicMethods ru.ozon.test.ComponentA {
                    ignore fun get(): String
                }
            """.trimIndent()
        )

        val result = gradleRunner.runWithLog(":app:assembleDebug")

        assertTrue(
            resultHasTaskWithOutcome(result, validateTaskPath, TaskOutcome.SUCCESS),
            "Конфиг с корректным ignore должен успешно пройти валидацию"
        )

        val methodsWithInjection = patchedMethodsWithInjection()
        assertTrue(
            methodsWithInjection["get"] == false,
            "Метод get() подпадает под ignore и не должен содержать вызов инициалайзера"
        )
        assertTrue(
            methodsWithInjection["initialize"] == true,
            "Метод initialize() не подпадает под ignore и должен быть пропатчен"
        )
    }

    /** Метод -> содержит ли он встроенный вызов getComponentInitializer */
    private fun patchedMethodsWithInjection(): Map<String, Boolean> {
        val methods = mutableMapOf<String, Boolean>()
        val reader = ClassReader(patchedComponentFile.readBytes())

        reader.accept(object : ClassVisitor(Opcodes.ASM9) {
            override fun visitMethod(
                access: Int,
                name: String?,
                descriptor: String?,
                signature: String?,
                exceptions: Array<out String>?,
            ): MethodVisitor {
                if (name == null) return super.visitMethod(access, name, descriptor, signature, exceptions)

                methods.putIfAbsent(name, false)
                return object : MethodVisitor(Opcodes.ASM9) {
                    override fun visitMethodInsn(
                        opcode: Int,
                        owner: String?,
                        methodName: String?,
                        descriptor: String?,
                        isInterface: Boolean,
                    ) {
                        if (methodName == "getComponentInitializer") {
                            methods[name] = true
                        }
                        super.visitMethodInsn(opcode, owner, methodName, descriptor, isInterface)
                    }
                }
            }
        }, 0)
        return methods
    }

}
