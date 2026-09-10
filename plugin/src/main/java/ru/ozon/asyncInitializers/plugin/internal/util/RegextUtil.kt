package ru.ozon.asyncInitializers.plugin.internal.util

import ru.ozon.asyncInitializers.plugin.internal.exceptions.CannotParseLineException
import ru.ozon.asyncInitializers.plugin.internal.exceptions.NonConsistentLineException
import ru.ozon.asyncInitializers.plugin.internal.reader.LineReader

internal const val classPath = "[\\w_]+(?:\\.[\\w_]+)*(?:\\$[\\w_]+)*"
internal const val LamdaPattern = "Lamda"
internal const val CommaSeparatorChar = ','

internal fun Regex.findCurrentLineOrException(lineReader: LineReader): MatchResult {
    val currentLine = lineReader.currentLine ?: throw NonConsistentLineException()
    return find(currentLine) ?: throw CannotParseLineException(currentLine)
}
