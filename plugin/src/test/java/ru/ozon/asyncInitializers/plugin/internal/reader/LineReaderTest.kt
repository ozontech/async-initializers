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
    fun `пустые и бланковые строки пропускаются при чтении`() {
        val reader = readerFor("\n\n   \nпервая\n\n\nвторая\n")

        assertEquals("первая", reader.nextLine())
        assertEquals("вторая", reader.nextLine())
    }

    @Test
    fun `в конце потока currentLine и nextLine возвращают null`() {
        val reader = readerFor("строка")

        assertEquals("строка", reader.currentLine)
        assertNull(reader.nextLine())
        assertNull(reader.currentLine)
    }
}
