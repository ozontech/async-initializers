package ru.ozon.asyncInitializers.plugin.internal.asm.parcer

import org.junit.jupiter.api.Test
import org.objectweb.asm.Opcodes.ACC_PUBLIC
import ru.ozon.asyncInitializers.plugin.internal.asm.instances.MethodInstance
import ru.ozon.asyncInitializers.plugin.internal.exceptions.CannotParseDescriptorForMethodException
import ru.ozon.asyncInitializers.plugin.internal.image.images.TypeImage
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

private fun MethodInstance.methodImage() = createMethodImage()

internal class MethodInstanceParserTest {

    @Test
    fun `метод без параметров с void возвращает пустой список параметров`() {
        val image = MethodInstance(ACC_PUBLIC, "run", "()V").methodImage()

        assertEquals("run", image.name)
        assertEquals(emptyList(), image.orderedParameters)
        assertEquals(TypeImage.Primitive.VOID, image.returnType)
    }

    @Test
    fun `примитивные типы в параметрах распознаются`() {
        val image = MethodInstance(ACC_PUBLIC, "m", "(ZJ)V").methodImage()

        assertEquals(
            listOf(TypeImage.Primitive.BOOLEAN, TypeImage.Primitive.LONG),
            image.orderedParameters
        )
        assertEquals(TypeImage.Primitive.VOID, image.returnType)
    }

    @Test
    fun `объектные типы распознаются по jvm дескриптору`() {
        val image = MethodInstance(ACC_PUBLIC, "m", "(Ljava/lang/String;)Lcom/foo/Bar;").methodImage()

        assertEquals(listOf(TypeImage.Object("java.lang.String")), image.orderedParameters)
        assertEquals(TypeImage.Object("com.foo.Bar"), image.returnType)
    }

    @Test
    fun `обёртка над примитивом сворачивается в примитивный тип`() {
        val image = MethodInstance(ACC_PUBLIC, "m", "()Ljava/lang/Integer;").methodImage()

        assertEquals(TypeImage.Primitive.INTEGER, image.returnType)
    }

    // LAMDA_JVM_PATTERN в проде мёртвый: содержит ведущую 'L' и ';', а javaName их лишён,
    // поэтому лямбда-дескриптор фактически парсится как обычный объект.
    @Test
    fun `лямбда-дескриптор парсится как обычный объект`() {
        val image = MethodInstance(ACC_PUBLIC, "m", "(Lkotlin/jvm/functions/Function1;)V").methodImage()

        assertEquals(listOf(TypeImage.Object("kotlin.jvm.functions.Function1")), image.orderedParameters)
    }

    @Test
    fun `массив примитивов распознаётся как тип-массив`() {
        val image = MethodInstance(ACC_PUBLIC, "m", "([I)V").methodImage()

        assertEquals(listOf(TypeImage.Array(TypeImage.Primitive.INTEGER)), image.orderedParameters)
        assertEquals(TypeImage.Primitive.VOID, image.returnType)
    }

    @Test
    fun `некорректный символ в дескрипторе кидает исключение`() {
        val exception = assertFailsWith<CannotParseDescriptorForMethodException> {
            MethodInstance(ACC_PUBLIC, "m", "(X)V").methodImage()
        }

        assertIs<CannotParseDescriptorForMethodException>(exception)
    }
}
