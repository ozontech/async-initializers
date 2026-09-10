package ru.ozon.asyncInitializers.plugin.internal.transfromResult

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor
import ru.ozon.asyncInitializers.plugin.internal.asm.instances.MethodInstance
import ru.ozon.asyncInitializers.plugin.internal.image.images.TypeImage

internal class InjectInitializerValidateClassVisitor(
    api: Int,
    classWriter: ClassWriter,
    private val builder: PatchingResult.Builder
): ClassVisitor(api, classWriter) {

    private var typeObject: TypeImage.Object? = null

    override fun visit(
        version: Int,
        access: Int,
        name: String,
        signature: String?,
        superName: String?,
        interfaces: Array<out String?>?
    ) {
        typeObject = TypeImage.Object(name.replace("/","."))
        super.visit(version, access, name, signature, superName, interfaces)
    }

    override fun visitMethod(
        access: Int,
        name: String,
        descriptor: String,
        signature: String?,
        exceptions: Array<out String?>?
    ): MethodVisitor {
        val methodInstance = MethodInstance(access, name, descriptor)

        val mv = super.visitMethod(access, name, descriptor, signature, exceptions)
        return InjectInitializerValidateMethodVisitor(
            api = api,
            type = checkNotNull(typeObject),
            methodInstance = methodInstance,
            builder = builder,
            nextVisitor = mv
        )
    }
}
