package ru.ozon.asyncInitializers.plugin.internal.asm.parcer

import ru.ozon.asyncInitializers.plugin.internal.asm.instances.MethodInstance
import ru.ozon.asyncInitializers.plugin.internal.exceptions.CannotParseDescriptorException
import ru.ozon.asyncInitializers.plugin.internal.exceptions.CannotParseDescriptorForMethodException
import ru.ozon.asyncInitializers.plugin.internal.image.images.MethodImage
import ru.ozon.asyncInitializers.plugin.internal.image.images.TypeImage

internal fun MethodInstance.createMethodImage(): MethodImage {
    val parsedDescriptor = runCatching { parseDescriptor(descriptor) }
        .getOrElse { error -> throw CannotParseDescriptorForMethodException(name, descriptor, error) }

    return MethodImage(
        name = name,
        orderedParameters = parsedDescriptor.dropLast(1),
        returnType = parsedDescriptor.last()
    )
}

private val backets = setOf('(',')')

private fun parseDescriptor(desc: String): List<TypeImage> {
    val types = mutableListOf<TypeImage>()
    val iterator = desc.iterator()
    while (iterator.hasNext()){
        val item = iterator.next()

        if (item.isWhitespace() || item in backets) continue

        val type = iterator.readNextObjectOrNull(item)
        if (type != null) {
            types.add(type)
            continue
        }

        throw CannotParseDescriptorException(desc, item)
    }

    return types
}

private fun replaceObjectIfNeed(typeObject: TypeImage.Object): TypeImage {
    val primitiveType = TypeImage.Primitive.values().find { type -> type.wrapperJavaName == typeObject }
    if (primitiveType != null) return primitiveType

    if (TypeImage.Lamda.LAMDA_JVM_PATTERN.matches(typeObject.javaName)) {
        return TypeImage.Lamda(typeObject.javaName)
    }

    return typeObject
}

private fun isObject(item: Char): Boolean = item == 'L'

private fun isArray(item: Char): Boolean = item == '['

private fun parsePrimitive(item: Char): TypeImage.Primitive? {
    val itemStr = item.toString()
    return TypeImage.Primitive.values().find { type -> type.jvmName == itemStr }
}

private fun CharIterator.readNextObjectOrNull(currentItem: Char): TypeImage? {

    if (isObject(currentItem)) {
        val typeObject = parseObject()
        return replaceObjectIfNeed(typeObject)
    }

    val primitiveType = parsePrimitive(currentItem)
    if (primitiveType != null) {
        return primitiveType
    }

    if (isArray(currentItem) && hasNext()) {
        val type = readNextObjectOrNull(next())
        if (type != null) return TypeImage.Array(type)
    }

    return null
}

private fun CharIterator.parseObject(): TypeImage.Object {
    val javaNameObject = StringBuilder()
    while (hasNext()) {
        var item = next()
        if (item == ';') break
        if (item == '/') item = '.'

        javaNameObject.append(item)
    }

    return TypeImage.Object(javaNameObject.toString())
}
