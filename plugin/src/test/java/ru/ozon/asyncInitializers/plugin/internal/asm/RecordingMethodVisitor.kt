package ru.ozon.asyncInitializers.plugin.internal.asm

import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes.ASM9
import org.objectweb.asm.Type

/**
 * Test MethodVisitor that records the instructions of interest,
 * allowing to verify bytecode emitted by modifying visitors.
 */
internal class RecordingMethodVisitor : MethodVisitor(ASM9) {

    val ldcTypes = mutableListOf<Type>()
    val checkcasts = mutableListOf<String>()
    val methodCalls = mutableListOf<String>()

    /** Collects types from LDC instructions — usually loading the initializer class via `Type`. */
    override fun visitLdcInsn(value: Any?) {
        (value as? Type)?.let { ldcTypes.add(it) }
        super.visitLdcInsn(value)
    }

    /** Collects type names from type instructions (CHECKCAST, NEW, ANEWARRAY, etc.) — e.g. cast to the initializer type. */
    override fun visitTypeInsn(opcode: Int, type: String?) {
        type?.let { checkcasts.add(it) }
        super.visitTypeInsn(opcode, type)
    }

    /** Collects method calls as `opcode owner name descriptor` — for verifying initializer calls. */
    override fun visitMethodInsn(
        opcode: Int,
        owner: String?,
        name: String?,
        descriptor: String?,
        isInterface: Boolean,
    ) {
        methodCalls.add("$opcode $owner $name $descriptor")
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
    }
}
