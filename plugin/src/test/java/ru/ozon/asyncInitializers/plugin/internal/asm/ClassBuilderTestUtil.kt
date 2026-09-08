package ru.ozon.asyncInitializers.plugin.internal.asm

import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes

internal class MethodSpec(
    val access: Int,
    val name: String,
    val descriptor: String,
)

/**
 * Сборка простого class-файла в памяти для тестов ASM-визиторов (без компиляции исходников).
 */
internal object ClassBuilderTestUtil {

    fun buildClass(
        className: String,
        access: Int = Opcodes.ACC_PUBLIC,
        superName: String = "java/lang/Object",
        interfaces: Array<String> = emptyArray(),
        methods: List<MethodSpec> = emptyList(),
    ): ByteArray {
        val writer = ClassWriter(0)
        writer.visit(Opcodes.V1_8, access, className, null, superName, interfaces)

        methods.forEach { spec ->
            val mv = writer.visitMethod(spec.access, spec.name, spec.descriptor, null, null)
            if ((spec.access and Opcodes.ACC_ABSTRACT) == 0) {
                mv.visitCode()
                mv.visitInsn(Opcodes.RETURN)
                mv.visitMaxs(0, 0)
            }
            mv.visitEnd()
        }

        writer.visitEnd()
        return writer.toByteArray()
    }
}
