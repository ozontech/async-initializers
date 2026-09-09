package ru.ozon.asyncInitializers.plugin.internal.patchingController

import com.android.build.api.instrumentation.ClassData
import ru.ozon.asyncInitializers.plugin.internal.image.images.ModifiedImage
import ru.ozon.asyncInitializers.plugin.internal.util.containsVictimByName

/**
 * Entity responsible for checking whether a class needs embedding
 * [ComponentInitializer] into its methods
 */
internal class ProgramPatchingController private constructor(
    val modifiedImages: List<ModifiedImage>,
) {
    fun shouldPatch(classData: ClassData): Boolean {
        return modifiedImages.containsVictimByName(classData.className)
    }

    fun createClassPatchController(classData: ClassData): ClassPatchingController {
        val victimModifiedImages = modifiedImages
            .filter { image -> image.victim.javaName == classData.className }

        return ClassPatchingController.Companion.create(
            modifiedImages = victimModifiedImages,
        )
    }

    class Builder() {
        val modifiedImages = mutableListOf<ModifiedImage>()

        fun addType(modifiedImage: ModifiedImage) = apply { modifiedImages += modifiedImage }

        fun build(): ProgramPatchingController {
            return ProgramPatchingController(modifiedImages.toList())
        }
    }
}
