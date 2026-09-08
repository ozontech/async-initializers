package ru.ozon.asyncInitializers.plugin.internal.reader

import java.io.BufferedReader
import java.io.File

internal fun File.lineReadFile(action: LineReader.() -> Unit) {
    bufferedReader().use { reader -> action(LineReader(reader)) }
}

/**
 * Ридер потока, считывающий поток построчно
 *
 * Также способен пропускать пустые строки
 */
internal class LineReader(private val bufferedReader: BufferedReader) {
    var currentLine: String? = null
        get() = if (field == null) nextLine() else field

        private set

    fun nextLine(): String? {
        var line = bufferedReader.readLine()
        while (line != null && line.isBlank()) {
            line = bufferedReader.readLine()
        }
        currentLine = line
        return line
    }
}
