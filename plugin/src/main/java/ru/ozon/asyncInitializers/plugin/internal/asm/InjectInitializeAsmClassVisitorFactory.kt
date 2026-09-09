package ru.ozon.asyncInitializers.plugin.internal.asm

import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import org.gradle.api.tasks.Internal
import org.objectweb.asm.ClassVisitor
import ru.ozon.asyncInitializers.plugin.internal.parser.ConfigFileParser
import ru.ozon.asyncInitializers.plugin.internal.patchingController.ProgramPatchingController
import ru.ozon.asyncInitializers.plugin.internal.util.isComponentInitializer
import java.util.WeakHashMap

/**
 * Code-patching mechanism using ASM and supported by Gradle out of the box
 */
internal abstract class InjectInitializeAsmClassVisitorFactory: AsmClassVisitorFactory<InjectInstrumentationParameters> {

    @get:Internal
    val programPatchingController: ProgramPatchingController
        get() = synchronized(lock) {
            cachedControllers.getOrPut(this) {
                val config = ConfigFileParser.readModifiedTypes(parameters.getConfigsFile())
                ConfigFileParser.createProgramPatchingController(config)
            }
        }

    /**
     * Once [isInstrumentable] passes, class patching begins
     */
    override fun createClassVisitor(
        classContext: ClassContext,
        nextClassVisitor: ClassVisitor
    ): ClassVisitor {
        val classPatchingController = programPatchingController
            .createClassPatchController(classContext.currentClassData)

        return InjectInitializeClassVisitor(
            api = instrumentationContext.apiVersion.get(),
            nextClassVisitor = nextClassVisitor,
            classPatchingController = classPatchingController,
        )
    }

    /**
     * Checks whether this class needs patching
     */
    override fun isInstrumentable(classData: ClassData): Boolean {
        return (!classData.isComponentInitializer() && programPatchingController.shouldPatch(classData))
    }

    companion object {
        private val lock = Any()
        private val cachedControllers = WeakHashMap<Any, ProgramPatchingController>()
    }
}
