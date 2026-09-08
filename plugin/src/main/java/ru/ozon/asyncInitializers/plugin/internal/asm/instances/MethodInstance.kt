package ru.ozon.asyncInitializers.plugin.internal.asm.instances

import org.objectweb.asm.Opcodes.ACC_PUBLIC

private val constructorsName = setOf("<init>","<clinit>")

/**
 * Представление метода в ASM
 */
internal data class MethodInstance(
    private val access: Int,
    val name: String,
    val descriptor: String,
) {
    val isPublic = (access and ACC_PUBLIC) != 0
    val isConstructor = name in constructorsName
}
