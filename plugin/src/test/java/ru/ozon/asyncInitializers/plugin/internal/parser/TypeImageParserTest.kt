package ru.ozon.asyncInitializers.plugin.internal.parser

import org.junit.jupiter.api.Test
import ru.ozon.asyncInitializers.plugin.internal.image.images.TypeImage
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

internal class TypeImageParserTest {

    @Test
    fun `primitives and Unit fold into primitive types`() {
        assertEquals(TypeImage.Primitive.INTEGER, "Int".toTypeImage())
        assertEquals(TypeImage.Primitive.BOOLEAN, "Boolean".toTypeImage())
        assertEquals(TypeImage.Primitive.VOID, "Unit".toTypeImage())
    }

    @Test
    fun `String folds into an object`() {
        assertEquals(TypeImage.Object("java.lang.String"), "String".toTypeImage())
    }

    @Test
    fun `lambda with type parameters is recognized`() {
        val image = "Lamda<com.a.A, com.b.B>".toTypeImage()

        assertEquals(TypeImage.Lamda(countParams = 2), image)
        assertEquals("Lkotlin.jvm.functions.Function2;", image.jvmName)
    }

    @Test
    fun `fully qualified class name becomes an object`() {
        assertEquals(TypeImage.Object("com.foo.Bar"), "com.foo.Bar".toTypeImage())
    }

    @Test
    fun `checkOnObject returns the passed object`() {
        val objectImage = TypeImage.Object("com.foo.Bar")
        assertSame(objectImage, checkOnObject(objectImage))
    }

    @Test
    fun `checkOnObject throws on a non-object`() {
        assertFailsWith<IllegalStateException> { checkOnObject(TypeImage.Primitive.INTEGER) }
    }
}
