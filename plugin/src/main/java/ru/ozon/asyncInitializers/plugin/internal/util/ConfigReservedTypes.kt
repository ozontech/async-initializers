package ru.ozon.asyncInitializers.plugin.internal.util

import ru.ozon.asyncInitializers.plugin.internal.image.images.TypeImage

internal val ConfigReservedTypes = OneToOne<String, TypeImage>(
    "Int" to TypeImage.Primitive.INTEGER,
    "Short" to TypeImage.Primitive.SHORT,
    "Boolean" to TypeImage.Primitive.BOOLEAN,
    "Byte" to TypeImage.Primitive.BYTE,
    "Long" to TypeImage.Primitive.LONG,
    "Char" to TypeImage.Primitive.CHAR,
    "Float" to TypeImage.Primitive.FLOAT,
    "Double" to TypeImage.Primitive.DOUBLE,
    "Unit" to TypeImage.Primitive.VOID,
    "String" to TypeImage.Object("java.lang.String")
)

internal class OneToOne<T,K>(vararg pairs: Pair<T,K>) {
    private val firstAssociated = pairs.associateBy(
        keySelector = { it.first },
        valueTransform = { it.second }
    )

    private val secondAssociated = pairs.associateBy(
        keySelector = { it.second },
        valueTransform = { it.first }
    )

    fun findFirstAssociated(value: T): K? = firstAssociated[value]

    fun findSecondAssociated(value: K): T? = secondAssociated[value]

}
