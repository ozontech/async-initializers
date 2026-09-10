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
    fun `parsing a method with object parameters and void`() {
        val image = parse("ignore fun configure(ru.ozon.example.api.AppConfig, ru.ozon.example.api.AppDependencies)")

        assertEquals("configure", image.name)
        assertEquals(
            listOf(
                TypeImage.Object("ru.ozon.example.api.AppConfig"),
                TypeImage.Object("ru.ozon.example.api.AppDependencies"),
            ),
            image.orderedParameters
        )
        assertEquals(TypeImage.Primitive.VOID, image.returnType)
    }

    @Test
    fun `the specified return type is preserved`() {
        val image = parse("ignore fun make(): com.foo.Bar")

        assertEquals(TypeImage.Object("com.foo.Bar"), image.returnType)
    }

    @Test
    fun `lambda parameter is not split by commas inside it`() {
        val image = parse("ignore fun apply(Lamda<com.a.X, com.b.Y>)")

        assertEquals(1, image.orderedParameters.size)
        assertEquals(TypeImage.Lamda(countParams = 2), image.orderedParameters[0])
    }

    @Test
    fun `method without parameters has an empty parameter list`() {
        val image = parse("ignore fun a()")

        assertEquals(emptyList(), image.orderedParameters)
        assertEquals(TypeImage.Primitive.VOID, image.returnType)
    }

    @Test
    fun `reserved types fold into primitives and string`() {
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
    fun `mismatched line is not parsed and returns null`() {
        assertNull(parser.createImageOrNull(readerFor("just a garbage line")))
    }
}
