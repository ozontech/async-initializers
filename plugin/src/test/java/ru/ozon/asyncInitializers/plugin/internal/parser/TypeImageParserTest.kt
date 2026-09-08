package ru.ozon.asyncInitializers.plugin.internal.parser

import org.junit.jupiter.api.Test
import ru.ozon.asyncInitializers.plugin.internal.image.images.TypeImage
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

internal class TypeImageParserTest {

    @Test
    fun `примитивы и Unit сворачиваются в примитивные типы`() {
        assertEquals(TypeImage.Primitive.INTEGER, "Int".toTypeImage())
        assertEquals(TypeImage.Primitive.BOOLEAN, "Boolean".toTypeImage())
        assertEquals(TypeImage.Primitive.VOID, "Unit".toTypeImage())
    }

    @Test
    fun `String сворачивается в объект`() {
        assertEquals(TypeImage.Object("java.lang.String"), "String".toTypeImage())
    }

    @Test
    fun `лямбда с параметрами типов распознаётся`() {
        val image = "Lamda<com.a.A, com.b.B>".toTypeImage()

        assertEquals(TypeImage.Lamda(countParams = 2), image)
        assertEquals("Lkotlin.jvm.functions.Function2;", image.jvmName)
    }

    @Test
    fun `полное имя класса становится объектом`() {
        assertEquals(TypeImage.Object("com.foo.Bar"), "com.foo.Bar".toTypeImage())
    }

    @Test
    fun `checkOnObject возвращает переданный объект`() {
        val objectImage = TypeImage.Object("com.foo.Bar")
        assertSame(objectImage, checkOnObject(objectImage))
    }

    @Test
    fun `checkOnObject кидает исключение на не-объекте`() {
        assertFailsWith<IllegalStateException> { checkOnObject(TypeImage.Primitive.INTEGER) }
    }
}
