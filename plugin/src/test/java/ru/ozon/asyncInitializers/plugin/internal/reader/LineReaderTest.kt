package ru.ozon.asyncInitializers.plugin.internal.reader

import org.junit.jupiter.api.Test
import java.io.BufferedReader
import java.io.StringReader
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class LineReaderTest {

    private fun readerFor(content: String): LineReader =
        LineReader(BufferedReader(StringReader(content)))

    @Test
    fun `empty and blank lines are skipped when reading`() {
        val reader = readerFor("\n\n   \nfirst\n\n\nsecond\n")

        assertEquals("first", reader.nextLine())
        assertEquals("second", reader.nextLine())
    }

    @Test
    fun `currentLine and nextLine return null at the end of the stream`() {
        val reader = readerFor("line")

        assertEquals("line", reader.currentLine)
        assertNull(reader.nextLine())
        assertNull(reader.currentLine)
    }
}
