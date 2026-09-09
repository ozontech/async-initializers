package ru.ozon.asyncInitializers.plugin.internal.parser.lineParsers

import ru.ozon.asyncInitializers.plugin.internal.exceptions.CannotParseLineException
import ru.ozon.asyncInitializers.plugin.internal.image.images.MethodImage
import ru.ozon.asyncInitializers.plugin.internal.image.images.ModifiedImage
import ru.ozon.asyncInitializers.plugin.internal.parser.LineParser
import ru.ozon.asyncInitializers.plugin.internal.parser.checkOnObject
import ru.ozon.asyncInitializers.plugin.internal.parser.toTypeImage
import ru.ozon.asyncInitializers.plugin.internal.reader.LineReader
import ru.ozon.asyncInitializers.plugin.internal.util.classPath
import ru.ozon.asyncInitializers.plugin.internal.util.findCurrentLineOrException

/**
 * Parser for a config line
 *
 * Example: "inject ru.ozon.hire.initializer.LimbInitializer toPublicMethods ru.ozon.limb.api.LimbAppApi {"
 */
internal class ModifiedLineParser: LineParser<ModifiedImage>() {
    private val LINE_PARSE_REGEX = Regex("^inject\\s+($classPath)\\s+to[pP]ublic[mM]ethods\\s+($classPath)\\s*(\\{)?\\s*$")
    private val END_BLOCK_REGEX = Regex("^\\s*\\}\\s*")

    override fun canParse(line: String): Boolean {
        return LINE_PARSE_REGEX.matches(line)
    }

    override fun LineReader.createImage(): ModifiedImage {
        val matchResult = LINE_PARSE_REGEX.findCurrentLineOrException(this)

        val initializerClass = matchResult.groupValues[1].toTypeImage()
        val modifiedClass = matchResult.groupValues[2].toTypeImage()
        val isBlock = matchResult.groupValues.getOrNull(3) != null

        val ignoredMethods = hashSetOf<MethodImage>()

        if (isBlock) {
            var line = nextLine()
            val ignoreMethodsParser = IgnoreMothodsParser()
            while (line != null && !END_BLOCK_REGEX.matches(line)) {
                val method = ignoreMethodsParser.createImageOrNull(this) ?: throw CannotParseLineException(line)
                ignoredMethods += method
                line = nextLine()
            }
        }

        return ModifiedImage(
            victim = checkOnObject(modifiedClass),
            initializer = checkOnObject(initializerClass),
            ignoredMethods = ignoredMethods.toSet()
        )
    }

}
