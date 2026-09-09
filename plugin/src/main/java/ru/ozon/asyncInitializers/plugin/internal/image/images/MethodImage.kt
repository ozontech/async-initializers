package ru.ozon.asyncInitializers.plugin.internal.image.images

import ru.ozon.asyncInitializers.plugin.internal.image.Image


/**
 * Representation of a method used in the JVM
 *
 * @param name method name
 * @param orderedParameters method parameters
 * @param returnType return type
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
