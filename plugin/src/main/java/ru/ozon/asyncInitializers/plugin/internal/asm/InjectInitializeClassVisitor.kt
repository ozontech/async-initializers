package ru.ozon.asyncInitializers.plugin.internal.asm

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes.ACC_INTERFACE
import ru.ozon.asyncInitializers.plugin.internal.asm.instances.MethodInstance
import ru.ozon.asyncInitializers.plugin.internal.exceptions.PatchingInterfaceException
import ru.ozon.asyncInitializers.plugin.internal.patchingController.ClassPatchingController
import ru.ozon.asyncInitializers.plugin.internal.util.WasInjectInitializerAnnotation

internal class InjectInitializeClassVisitor(
    api: Int,
    nextClassVisitor: ClassVisitor,
    private val classPatchingController: ClassPatchingController,
): ClassVisitor(api, nextClassVisitor) {

    override fun visit(
        version: Int,
        access: Int,
        name: String,
        signature: String?,
        superName: String?,
        interfaces: Array<out String?>?
    ) {
        if (isInterface(access)) throw PatchingInterfaceException(name)
        super.visit(version, access, name, signature, superName, interfaces)
    }

    override fun visitMethod(
        access: Int,
        name: String,
        descriptor: String,
        signature: String?,
        exceptions: Array<out String?>?
    ): MethodVisitor? {
        val methodInstance = MethodInstance(access, name, descriptor)
        val mv = super.visitMethod(access, name, descriptor, signature, exceptions)

        if (!classPatchingController.shouldPatch(methodInstance)) {
            return mv
        }

        mv.markAsPatched()
        val methodPatchingController = classPatchingController.createMethodPatchController(methodInstance)
        return InjectInitializeMethodVisitor(api, mv, methodPatchingController)
    }

    private fun isInterface(access: Int) = (access and ACC_INTERFACE) != 0

    /**
     * Пометка метода маркером (аннотацией @WasInjectInitializer), что метод был модифицирован
     */
    private fun MethodVisitor.markAsPatched() {
        visitAnnotation(WasInjectInitializerAnnotation.jvmName, true)?.visitEnd()
    }
}
