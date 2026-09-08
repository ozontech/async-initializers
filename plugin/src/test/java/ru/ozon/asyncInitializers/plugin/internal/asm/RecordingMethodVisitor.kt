package ru.ozon.asyncInitializers.plugin.internal.asm

import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes.ASM9
import org.objectweb.asm.Type

/**
 * Тестовый MethodVisitor, записывающий интересующие нас инструкции,
 * позволяющий проверять байткод, эмитируемый модифицирующими визиторами.
 */
internal class RecordingMethodVisitor : MethodVisitor(ASM9) {

    val ldcTypes = mutableListOf<Type>()
    val checkcasts = mutableListOf<String>()
    val methodCalls = mutableListOf<String>()

    /** Собирает типы из LDC-инструкций — обычно это загрузка класса инициалайзера через `Type`. */
    override fun visitLdcInsn(value: Any?) {
        (value as? Type)?.let { ldcTypes.add(it) }
        super.visitLdcInsn(value)
    }

    /** Собирает имена типов из type-инструкций (CHECKCAST, NEW, ANEWARRAY и т.п.) — например, каст к типу инициалайзера. */
    override fun visitTypeInsn(opcode: Int, type: String?) {
        type?.let { checkcasts.add(it) }
        super.visitTypeInsn(opcode, type)
    }

    /** Собирает вызовы методов в виде `opcode owner name descriptor` — для проверки вызовов инициалайзеров. */
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
