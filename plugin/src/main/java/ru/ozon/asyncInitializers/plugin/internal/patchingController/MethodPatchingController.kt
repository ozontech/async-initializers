package ru.ozon.asyncInitializers.plugin.internal.patchingController

import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes.CHECKCAST
import org.objectweb.asm.Opcodes.INVOKESTATIC
import org.objectweb.asm.Opcodes.INVOKEVIRTUAL
import org.objectweb.asm.Type
import ru.ozon.asyncInitializers.plugin.internal.util.InitializerTypeImage

/**
 * Модификтор метода встраивает [ru.ozon.asyncInitializers.plugin.internal.util.ComponentInitializer] в код
 */
internal class MethodPatchingController private constructor(
    private val initializerTypeImages: List<InitializerTypeImage>,
) {

    /**
     * Встраивание следующей строки
     * "(getComponentInitializer(T::class.java) as T).initialize()"
     *
     * Где T - наш инициалайзер
     */
    fun injectInitializers(methodVisitor: MethodVisitor) = with(methodVisitor) {
        initializerTypeImages.forEach { initializer ->
            val typeRefName = initializer.javaName.replace(".","/")
            visitLdcInsn(Type.getObjectType(typeRefName))
            visitMethodInsn(
                INVOKESTATIC,
                "ru/ozon/asyncInitializer/library/AppComponentInitializerUtilsKt",
                "getComponentInitializer",
                "(Ljava/lang/Class;)Lru/ozon/asyncInitializer/library/ComponentInitializer;",
                false
            )
            visitTypeInsn(
                CHECKCAST,
                typeRefName
            )
            visitMethodInsn(
                INVOKEVIRTUAL,
                typeRefName,
                "initialize",
                "()V",
                false
            )
        }
    }

    companion object {
        fun create(
            initializerTypeImages: List<InitializerTypeImage>,
        ): MethodPatchingController {
            return MethodPatchingController(initializerTypeImages)
        }
    }
}
