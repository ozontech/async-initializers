package ru.ozon.asyncInitializers.plugin.internal.transfromResult

import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.MethodVisitor
import ru.ozon.asyncInitializers.plugin.internal.asm.instances.MethodInstance
import ru.ozon.asyncInitializers.plugin.internal.image.images.TypeImage
import ru.ozon.asyncInitializers.plugin.internal.util.WasInjectInitializerAnnotation

/**
 * ASM модификатор методов, который в данной реализации не модифицирует код,
 * а ищет аннотацию-маркер, сообщающую, что метод был модифицирован
 */
internal class InjectInitializerValidateMethodVisitor(
    api: Int,
    nextVisitor: MethodVisitor,
    private val type: TypeImage.Object,
    private val methodInstance: MethodInstance,
    private val builder: PatchingResult.Builder
): MethodVisitor(api, nextVisitor) {

    private var wasMarkAsPatched = false

    override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor? {
        if (WasInjectInitializerAnnotation.jvmName == descriptor) {
            builder.addPatchedMethod(type, methodInstance)
            wasMarkAsPatched = true
        }

        return super.visitAnnotation(descriptor, visible)
    }

    override fun visitEnd() {
        if (!wasMarkAsPatched) {
            builder.addIgnoreMethod(type, methodInstance)
        }
        super.visitEnd()
    }


}
