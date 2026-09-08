package ru.ozon.asyncInitializers.plugin.internal.parser

import ru.ozon.asyncInitializers.plugin.internal.image.images.TypeImage
import ru.ozon.asyncInitializers.plugin.internal.util.ConfigReservedTypes
import ru.ozon.asyncInitializers.plugin.internal.util.LamdaPattern
import ru.ozon.asyncInitializers.plugin.internal.util.classPath


private val LAMDA_REGEX = Regex("^$LamdaPattern<(\\s?${classPath}\\s?(?:,\\s?${classPath})*\\s?)>$")

internal fun String.toTypeImage(): TypeImage {
    return toReservedTypeOrNull() ?: toLamdaOrNull() ?: TypeImage.Object(this)
}

internal fun checkOnObject(typeImage: TypeImage): TypeImage.Object {
    check(typeImage is TypeImage.Object) {
        "ожидался ${TypeImage::class.simpleName}:${TypeImage.Object::class.simpleName}, " +
                "но был получен ${typeImage.javaClass.simpleName} для ${typeImage.jvmName}"
    }

    return typeImage
}

private fun String.toReservedTypeOrNull(): TypeImage? {
    return ConfigReservedTypes.findFirstAssociated(this)
}

private fun String.toLamdaOrNull(): TypeImage.Lamda? {
    val matchResult = LAMDA_REGEX.find(this) ?: return null

    val lamdaParamsCount = matchResult.groupValues[1].split(",").size

    return TypeImage.Lamda(
        countParams = lamdaParamsCount
    )
}
