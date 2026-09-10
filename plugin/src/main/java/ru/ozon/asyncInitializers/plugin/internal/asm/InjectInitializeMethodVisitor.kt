package ru.ozon.asyncInitializers.plugin.internal.asm

import org.objectweb.asm.MethodVisitor
import ru.ozon.asyncInitializers.plugin.internal.patchingController.MethodPatchingController

internal class InjectInitializeMethodVisitor(
    api: Int,
    private val methodVisitor: MethodVisitor,
    private val methodPatchingController: MethodPatchingController,
): MethodVisitor(api, methodVisitor) {

    override fun visitCode() {
        super.visitCode()
        methodPatchingController.injectInitializers(methodVisitor)
    }

}
