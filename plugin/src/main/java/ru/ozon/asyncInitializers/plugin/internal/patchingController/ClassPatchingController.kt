package ru.ozon.asyncInitializers.plugin.internal.patchingController

import ru.ozon.asyncInitializers.plugin.internal.asm.instances.MethodInstance
import ru.ozon.asyncInitializers.plugin.internal.asm.parcer.createMethodImage
import ru.ozon.asyncInitializers.plugin.internal.image.images.ModifiedImage

/**
 * Entity responsible for checking whether a method needs patching
 * and creating a dedicated patcher for each method
 */
internal class ClassPatchingController private constructor(
    private val modifiedImages: List<ModifiedImage>,
) {

    fun shouldPatch(method: MethodInstance): Boolean {
        if (method.isConstructor || !method.isPublic) return false
        val methodImage = method.createMethodImage()

        return modifiedImages.any { image -> methodImage !in image.ignoredMethods }
    }

    fun createMethodPatchController(method: MethodInstance): MethodPatchingController {
        val methodImage = method.createMethodImage()

        return MethodPatchingController.Companion.create(
            initializerTypeImages = modifiedImages
                .filter { image -> !image.ignoredMethods.contains(methodImage) }
                .map { image -> image.initializer },
        )
    }

    companion object {
        fun create(
            modifiedImages: List<ModifiedImage>,
        ): ClassPatchingController {
            return ClassPatchingController(modifiedImages)
        }
    }
}
