package ru.ozon.asyncInitializers.plugin.internal.parser

import ru.ozon.asyncInitializers.plugin.internal.image.Image
import ru.ozon.asyncInitializers.plugin.internal.reader.LineReader

internal abstract class LineParser<T: Image>() {

    fun createImageOrNull(lineReader: LineReader): T? {
        val currentLine = lineReader.currentLine ?: return null
        if (!canParse(currentLine)) return null
        return lineReader.createImage()
    }

    protected abstract fun canParse(line: String): Boolean
    protected abstract fun LineReader.createImage(): T
}
