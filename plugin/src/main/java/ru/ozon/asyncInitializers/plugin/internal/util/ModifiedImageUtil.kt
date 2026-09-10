package ru.ozon.asyncInitializers.plugin.internal.util

import ru.ozon.asyncInitializers.plugin.internal.image.images.ModifiedImage

internal fun List<ModifiedImage>.containsVictimByName(javaName: String): Boolean {
   return find { image ->
       image.victim.javaName == javaName || "${image.victim.javaName}Kt" == javaName
   } != null
}

internal fun List<ModifiedImage>.containsInitializerByName(javaName: String): Boolean {
    return find { image ->
        image.initializer.javaName == javaName || "${image.initializer.javaName}Kt" == javaName
    } != null
}
