package ru.ozon.asyncInitializers.plugin.internal.transfromResult

import ru.ozon.asyncInitializers.plugin.internal.asm.instances.MethodInstance
import ru.ozon.asyncInitializers.plugin.internal.asm.parcer.createMethodImage
import ru.ozon.asyncInitializers.plugin.internal.image.images.MethodImage
import ru.ozon.asyncInitializers.plugin.internal.image.images.ModifiedImage
import ru.ozon.asyncInitializers.plugin.internal.image.images.TypeImage

internal class PatchingResult private constructor(
    private val necessaryModifiedImages: List<ModifiedImage>,
    private val modifiedMethods: Map<TypeImage.Object, Set<MethodImage>>,
    private val ignoreModifiedMethods: Map<TypeImage.Object, Set<MethodImage>>,
    private val foundInitializers: Set<TypeImage.Object>
) {

    fun checkOnValidTransformation() {
        necessaryModifiedImages.forEach { modifiedType ->
            checkOnExitsClass(modifiedType.victim)
            checkOnExitsInitializer(modifiedType.initializer)
            checkOnIgnoredMethod(modifiedType.victim, modifiedType.ignoredMethods)
        }
    }

    private fun checkOnExitsClass(typeImage: TypeImage.Object) {
        check(modifiedMethods.contains(typeImage) || ignoreModifiedMethods.contains(typeImage)) {
            "Класс ${typeImage} не был модифицирован, поскольку не был найден"
        }
    }

    private fun checkOnIgnoredMethod(
        victim: TypeImage.Object,
        shouldIgnoredMethods: Set<MethodImage>,
    ) {
        val notFindForIgnore = shouldIgnoredMethods - ignoreModifiedMethods[victim].orEmpty()

        notFindForIgnore.forEach { notIgnoredMethod ->
            val errorMessage = buildString {
                append("Не смогли в классе ${victim} найти метод $notIgnoredMethod")
                append("\n\n")
                append("Другие найденые публичные методы в классе")
                append("\n")
                append(modifiedMethods[victim].orEmpty().joinToString("\n"))
            }
            error(errorMessage.toString())
        }
    }

    private fun checkOnExitsInitializer(typeImage: TypeImage.Object) {
        check(foundInitializers.contains(typeImage)) {
            "Инициалайзер ${typeImage} не был найден"
        }
    }

    class Builder(val necessaryModifiedImages: List<ModifiedImage>) {
        private val lock = Any()

        private val modifiedMethods = hashMapOf<TypeImage.Object, HashSet<MethodImage>>()
        private val ignoreModifiedMethods = hashMapOf<TypeImage.Object, HashSet<MethodImage>>()
        private val foundInitializers = hashSetOf<TypeImage.Object>()

        fun addFoundedInitializer(typeImage: TypeImage.Object) = synchronized(lock) {
            foundInitializers.add(typeImage)
        }

        fun addPatchedClass(typeImage: TypeImage.Object) = synchronized(lock) {
            ignoreModifiedMethods.getOrPut(typeImage) { hashSetOf() }
            modifiedMethods.getOrPut(typeImage) { hashSetOf() }
        }

        fun addPatchedMethod(typeImage: TypeImage.Object, methodInstance: MethodInstance) = synchronized(lock) {
            modifiedMethods.getOrPut(typeImage) { hashSetOf() }.add(methodInstance.createMethodImage())
        }

        fun addIgnoreMethod(typeImage: TypeImage.Object, methodInstance: MethodInstance) = synchronized(lock) {
            ignoreModifiedMethods.getOrPut(typeImage) { hashSetOf() }.add(methodInstance.createMethodImage())
        }

        fun build(): PatchingResult {
            return PatchingResult(
                necessaryModifiedImages = necessaryModifiedImages,
                modifiedMethods = modifiedMethods.mapValues { (_, set) -> set.toSet() },
                ignoreModifiedMethods = ignoreModifiedMethods.mapValues { (_, set) -> set.toSet() },
                foundInitializers = foundInitializers.toSet()
            )
        }
    }
}
