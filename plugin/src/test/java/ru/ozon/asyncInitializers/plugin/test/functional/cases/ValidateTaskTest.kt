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
 * Tests of the validate task (isTransformResultEnabled = true), invoked after patching
 * verifying that all classes declared in the config were found and patched.
 */
internal class ValidateTaskTest : BaseGradleProjectTest() {

    override val templateDir: File = File("src/test/templates/android-app")

    private val validateTaskPath = ":app:validateInitializersInjectDebug"

    @Test
    fun `validate task passes successfully after correct patching`() {
        val result = gradleRunner.runWithLog(":app:assembleDebug")

        assertTrue(
            resultHasTaskWithOutcome(result, validateTaskPath, TaskOutcome.SUCCESS),
            "After correct patching, $validateTaskPath should complete successfully"
        )
    }

    @Test
    fun `validate task fails if the victim class from the config is not found`() {
        setConfigContent(
            "inject ru.ozon.test.InitializerA toPublicMethods ru.ozon.test.MissingComponent"
        )

        val result = gradleRunner.runAndFailWithLog(":app:assembleDebug")

        assertTrue(
            resultHasTaskWithOutcome(result, validateTaskPath, TaskOutcome.FAILED),
            "Task $validateTaskPath should fail if the victim is not found"
        )
        assertTrue(
            result.output.contains("was not modified", ignoreCase = true),
            "Output should contain a message about an unfound victim class"
        )
    }

    @Test
    fun `validate task fails if the initializer from the config is not found`() {
        setConfigContent(
            "inject ru.ozon.test.MissingInitializer toPublicMethods ru.ozon.test.ComponentA"
        )

        val result = gradleRunner.runAndFailWithLog(":app:assembleDebug")

        assertTrue(
            resultHasTaskWithOutcome(result, validateTaskPath, TaskOutcome.FAILED),
            "Task $validateTaskPath should fail if the initializer is not found"
        )
        assertTrue(
            result.output.contains("was not found", ignoreCase = true),
            "Output should contain a message about an unfound initializer"
        )
    }

    @Test
    fun `validate task fails if ignore references a non-existent method`() {
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
            "Task $validateTaskPath should fail if ignore references a non-existent method"
        )
        assertTrue(
            result.output.contains("find method", ignoreCase = true),
            "Output should contain a message about an unfound ignore-method"
        )
    }

    @Test
    fun `ignore excludes the specified method from patching`() {
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
            "Config with a correct ignore should pass validation successfully"
        )

        val methodsWithInjection = patchedMethodsWithInjection()
        assertTrue(
            methodsWithInjection["get"] == false,
            "Method get() falls under ignore and should not contain an initializer call"
        )
        assertTrue(
            methodsWithInjection["initialize"] == true,
            "Method initialize() does not fall under ignore and should be patched"
        )
    }

    /** Method -> whether it contains an embedded getComponentInitializer call */
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
