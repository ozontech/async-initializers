package ru.ozon.asyncInitializers.plugin.internal.asm

import org.junit.jupiter.api.Test
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import ru.ozon.asyncInitializers.plugin.internal.image.images.TypeImage
import ru.ozon.asyncInitializers.plugin.internal.patchingController.MethodPatchingController
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class InjectInitializeMethodVisitorTest {

    private val initializer = TypeImage.Object("com.foo.MyInitializer")

    @Test
    fun `в начале метода встраивается вызов инициалайзера`() {
        val recorder = RecordingMethodVisitor()
        val controller = MethodPatchingController.create(listOf(initializer))
        val visitor = InjectInitializeMethodVisitor(Opcodes.ASM9, recorder, controller)

        visitor.visitCode()

        // ldc класса-инициалайзера
        assertEquals(listOf(Type.getObjectType("com/foo/MyInitializer")), recorder.ldcTypes)
        // CHECKCAST на инициалайзер
        assertEquals(listOf("com/foo/MyInitializer"), recorder.checkcasts)
        // getComponentInitializer(T::class.java)
        assertTrue(recorder.methodCalls.any { it.contains("getComponentInitializer") })
        // (инициалайзер as T).initialize()
        assertTrue(recorder.methodCalls.any { it.contains("initialize") && it.contains("()V") })
    }

    @Test
    fun `для нескольких инициалайзеров встраивается несколько вызовов`() {
        val recorder = RecordingMethodVisitor()
        val controller = MethodPatchingController.create(
            listOf(initializer, TypeImage.Object("com.foo.SecondInitializer"))
        )
        val visitor = InjectInitializeMethodVisitor(Opcodes.ASM9, recorder, controller)

        visitor.visitCode()

        assertEquals(2, recorder.ldcTypes.size)
        assertEquals(2, recorder.checkcasts.size)
        assertEquals(
            listOf("com/foo/MyInitializer", "com/foo/SecondInitializer"),
            recorder.checkcasts
        )
    }

    @Test
    fun `без инициалайзеров байткод не модифицируется`() {
        val recorder = RecordingMethodVisitor()
        val controller = MethodPatchingController.create(emptyList())
        val visitor = InjectInitializeMethodVisitor(Opcodes.ASM9, recorder, controller)

        visitor.visitCode()

        assertEquals(emptyList(), recorder.ldcTypes)
        assertEquals(emptyList(), recorder.checkcasts)
        assertEquals(emptyList(), recorder.methodCalls)
    }
}
