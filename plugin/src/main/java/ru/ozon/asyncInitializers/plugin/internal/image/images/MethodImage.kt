package ru.ozon.asyncInitializers.plugin.internal.image.images

import ru.ozon.asyncInitializers.plugin.internal.image.Image


/**
 * Образ метода используемого в JVM
 *
 * @param name имя метода
 * @param orderedParameters параметры метода
 * @param returnType возвращаемый тип
 */
internal data class MethodImage(
    val name: String,
    val orderedParameters: List<TypeImage>,
    val returnType: TypeImage
): Image {

    override fun toString(): String {
        return "fun $name(${orderedParameters.joinToString(", ")})${stringReturnTypeOrEmpty()}"
    }

    private fun stringReturnTypeOrEmpty(): String {
        if (returnType == TypeImage.Primitive.VOID) return ""
        return ": $returnType"
    }
}
