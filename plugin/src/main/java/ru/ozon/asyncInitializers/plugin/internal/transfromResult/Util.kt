package ru.ozon.asyncInitializers.plugin.internal.transfromResult

import ru.ozon.asyncInitializers.plugin.internal.image.images.TypeImage
import ru.ozon.asyncInitializers.plugin.internal.util.containsInitializerByName
import ru.ozon.asyncInitializers.plugin.internal.util.containsVictimByName

internal fun PatchingResult.Builder.isNeedValidate(classFileName: String): Boolean {
    val modifyName = classFileName
        .takeWhile { sym -> sym != '.' }
        .replace("/",".")

    val typeObject = TypeImage.Object(modifyName)

    if (isInitializer(typeObject)) {
        addFoundedInitializer(typeObject)
        return false
    }

    if (isVictim(typeObject)) {
        addPatchedClass(typeObject)
        return true
    }

    return false
}

private fun PatchingResult.Builder.isInitializer(type: TypeImage.Object): Boolean {
    return necessaryModifiedImages.containsInitializerByName(type.javaName)
}

private fun PatchingResult.Builder.isVictim(type: TypeImage.Object): Boolean {
    return necessaryModifiedImages.containsVictimByName(type.javaName)
}
