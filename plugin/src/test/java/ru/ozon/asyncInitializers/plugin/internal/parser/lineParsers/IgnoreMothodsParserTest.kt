package ru.ozon.asyncInitializers.plugin.internal.parser.lineParsers

import org.junit.jupiter.api.Test
import ru.ozon.asyncInitializers.plugin.internal.image.images.MethodImage
import ru.ozon.asyncInitializers.plugin.internal.image.images.TypeImage
import ru.ozon.asyncInitializers.plugin.internal.reader.LineReader
import java.io.BufferedReader
import java.io.StringReader
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class IgnoreMothodsParserTest {

    private val parser = IgnoreMothodsParser()

    private fun readerFor(line: String): LineReader =
        LineReader(BufferedReader(StringReader(line)))

    private fun parse(line: String): MethodImage =
        requireNotNull(parser.createImageOrNull(readerFor(line)))

    @Test
    fun `парсинг метода с объектными параметрами и void`() {
        val image = parse("ignore fun configure(ru.ozon.limb.api.LimbConfig, ru.ozon.limb.api.LimbDependencies)")

        assertEquals("configure", image.name)
        assertEquals(
            listOf(
                TypeImage.Object("ru.ozon.limb.api.LimbConfig"),
                TypeImage.Object("ru.ozon.limb.api.LimbDependencies"),
            ),
            image.orderedParameters
        )
        assertEquals(TypeImage.Primitive.VOID, image.returnType)
    }

    @Test
    fun `указанный return type сохраняется`() {
        val image = parse("ignore fun make(): com.foo.Bar")

        assertEquals(TypeImage.Object("com.foo.Bar"), image.returnType)
    }

    @Test
    fun `параметр-лямбда не разбивается по запятой внутри него`() {
        val image = parse("ignore fun apply(Lamda<com.a.X, com.b.Y>)")

        assertEquals(1, image.orderedParameters.size)
        assertEquals(TypeImage.Lamda(countParams = 2), image.orderedParameters[0])
    }

    @Test
    fun `метод без параметров имеет пустой список параметров`() {
        val image = parse("ignore fun a()")

        assertEquals(emptyList(), image.orderedParameters)
        assertEquals(TypeImage.Primitive.VOID, image.returnType)
    }

    @Test
    fun `зарезервированные типы сворачиваются в примитивы и строку`() {
        val image = parse("ignore fun a(Int, String, Unit)")

        assertEquals(
            listOf(
                TypeImage.Primitive.INTEGER,
                TypeImage.Object("java.lang.String"),
                TypeImage.Primitive.VOID,
            ),
            image.orderedParameters
        )
    }

    @Test
    fun `несоответствующая строка не парсится и возвращает null`() {
        assertNull(parser.createImageOrNull(readerFor("просто мусорная строка")))
    }
}
