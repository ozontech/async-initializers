package ru.ozon.asyncInitializers.plugin.internal.parser.lineParsers

import ru.ozon.asyncInitializers.plugin.internal.exceptions.CannotParseLineException
import ru.ozon.asyncInitializers.plugin.internal.exceptions.NonConsistentLineException
import ru.ozon.asyncInitializers.plugin.internal.image.images.MethodImage
import ru.ozon.asyncInitializers.plugin.internal.image.images.TypeImage
import ru.ozon.asyncInitializers.plugin.internal.parser.LineParser
import ru.ozon.asyncInitializers.plugin.internal.parser.toTypeImage
import ru.ozon.asyncInitializers.plugin.internal.reader.LineReader
import ru.ozon.asyncInitializers.plugin.internal.util.CommaSeparatorChar
import ru.ozon.asyncInitializers.plugin.internal.util.LamdaPattern

/**
 * Parser for a config line
 *
 * Example: "ignore fun configure(ru.ozon.limb.api.LimbConfig, ru.ozon.limb.api.LimbDependencies)"
 */
internal class IgnoreMothodsParser: LineParser<MethodImage>() {
    private val LINE_PARSE_REGEX = Regex("^\\s*ignore\\s+fun\\s+([\\w_]+)\\(([^)]*)\\)(?::\\s?(.*))?$")

    override fun canParse(line: String): Boolean = LINE_PARSE_REGEX.matches(line)

    override fun LineReader.createImage(): MethodImage {
        var line = currentLine ?: throw NonConsistentLineException()
        val matchResult = LINE_PARSE_REGEX.find(line) ?: throw CannotParseLineException(line)

        val name = matchResult.groupValues[1]
        val params = matchResult.groupValues[2].parseParams()
        val returnType = matchResult.groupValues[3].ifBlank { null }
            ?.let(String::toTypeImage)
            ?: TypeImage.Primitive.VOID

        return MethodImage(
            name = name,
            orderedParameters = params,
            returnType = returnType
        )
    }

    private fun String.parseParams(): List<TypeImage> {
        if (isBlank()) return emptyList()
        return smartSplitOnParams()
            .map { param -> param.toTypeImage() }
    }

    private fun String.smartSplitOnParams(): List<String> = buildList {
        var currentString = ""
        this@smartSplitOnParams.forEach { symbol ->
            if (symbol.isWhitespace()) return@forEach

            if (symbol == CommaSeparatorChar && currentString.startsWith(LamdaPattern)) {
                currentString += symbol
                return@forEach
            }

            if (symbol == CommaSeparatorChar) {
                add(currentString)
                currentString = ""
                return@forEach
            }

            currentString += symbol
        }
        add(currentString)
    }
}
