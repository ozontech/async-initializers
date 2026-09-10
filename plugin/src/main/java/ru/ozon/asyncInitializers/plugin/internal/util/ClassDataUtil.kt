package ru.ozon.asyncInitializers.plugin.internal.util

import com.android.build.api.instrumentation.ClassData
import ru.ozon.asyncInitializers.plugin.internal.image.images.TypeImage

internal fun ClassData.isComponentInitializer(): Boolean {
    return isSuperClassComponentInitializer() && isInheritorComponentInitializer()
}

internal fun ClassData.isInheritorComponentInitializer(): Boolean {
    return ComponentInitializer in superClasses
}

internal fun ClassData.isSuperClassComponentInitializer(): Boolean {
    return className == ComponentInitializer
}

internal fun ClassData.toTypeImageObject(): TypeImage.Object {
    return TypeImage.Object(className)
}
