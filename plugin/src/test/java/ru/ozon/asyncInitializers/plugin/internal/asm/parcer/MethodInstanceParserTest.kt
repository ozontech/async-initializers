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
    fun `method without parameters with void returns an empty parameter list`() {
        val image = MethodInstance(ACC_PUBLIC, "run", "()V").methodImage()

        assertEquals("run", image.name)
        assertEquals(emptyList(), image.orderedParameters)
        assertEquals(TypeImage.Primitive.VOID, image.returnType)
    }

    @Test
    fun `primitive parameter types are recognized`() {
        val image = MethodInstance(ACC_PUBLIC, "m", "(ZJ)V").methodImage()

        assertEquals(
            listOf(TypeImage.Primitive.BOOLEAN, TypeImage.Primitive.LONG),
            image.orderedParameters
        )
        assertEquals(TypeImage.Primitive.VOID, image.returnType)
    }

    @Test
    fun `object types are recognized by their jvm descriptor`() {
        val image = MethodInstance(ACC_PUBLIC, "m", "(Ljava/lang/String;)Lcom/foo/Bar;").methodImage()

        assertEquals(listOf(TypeImage.Object("java.lang.String")), image.orderedParameters)
        assertEquals(TypeImage.Object("com.foo.Bar"), image.returnType)
    }

    @Test
    fun `primitive wrapper is unwrapped into a primitive type`() {
        val image = MethodInstance(ACC_PUBLIC, "m", "()Ljava/lang/Integer;").methodImage()

        assertEquals(TypeImage.Primitive.INTEGER, image.returnType)
    }

    // LAMDA_JVM_PATTERN is dead in production: it contains a leading 'L' and ';' while javaName lacks them,
    // so a lambda descriptor is actually parsed as a regular object.
    @Test
    fun `lambda descriptor is parsed as a regular object`() {
        val image = MethodInstance(ACC_PUBLIC, "m", "(Lkotlin/jvm/functions/Function1;)V").methodImage()

        assertEquals(listOf(TypeImage.Object("kotlin.jvm.functions.Function1")), image.orderedParameters)
    }

    @Test
    fun `primitive array is recognized as an array type`() {
        val image = MethodInstance(ACC_PUBLIC, "m", "([I)V").methodImage()

        assertEquals(listOf(TypeImage.Array(TypeImage.Primitive.INTEGER)), image.orderedParameters)
        assertEquals(TypeImage.Primitive.VOID, image.returnType)
    }

    @Test
    fun `invalid character in descriptor throws an exception`() {
        val exception = assertFailsWith<CannotParseDescriptorForMethodException> {
            MethodInstance(ACC_PUBLIC, "m", "(X)V").methodImage()
        }

        assertIs<CannotParseDescriptorForMethodException>(exception)
    }
}
