package ru.ozon.asyncInitializers.plugin.internal.image.images

import ru.ozon.asyncInitializers.plugin.internal.image.Image
import ru.ozon.asyncInitializers.plugin.internal.util.InitializerTypeImage


internal data class ModifiedImage(
    val victim: TypeImage.Object,
    val initializer: InitializerTypeImage,
    val ignoredMethods: Set<MethodImage>
): Image
