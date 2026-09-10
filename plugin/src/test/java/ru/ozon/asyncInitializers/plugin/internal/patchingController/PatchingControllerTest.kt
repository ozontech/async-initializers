package ru.ozon.asyncInitializers.plugin.internal.patchingController

import com.android.build.api.instrumentation.ClassData
import org.junit.jupiter.api.Test
import org.objectweb.asm.Opcodes.ACC_PUBLIC
import ru.ozon.asyncInitializers.plugin.internal.asm.RecordingMethodVisitor
import ru.ozon.asyncInitializers.plugin.internal.asm.instances.MethodInstance
import ru.ozon.asyncInitializers.plugin.internal.image.images.MethodImage
import ru.ozon.asyncInitializers.plugin.internal.image.images.ModifiedImage
import ru.ozon.asyncInitializers.plugin.internal.image.images.TypeImage
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class PatchingControllerTest {

    private val victim = TypeImage.Object("com.foo.Victim")

    private val fooMethod = MethodImage("foo", emptyList(), TypeImage.Primitive.VOID)

    private fun modifiedImage(
        initializer: TypeImage.Object,
        ignoredMethods: Set<MethodImage> = emptySet(),
    ) = ModifiedImage(victim = victim, initializer = initializer, ignoredMethods = ignoredMethods)

    private fun classData(className: String): ClassData = object : ClassData {
        override val className: String = className
        override val classAnnotations: List<String> = emptyList()
        override val superClasses: List<String> = emptyList()
        override val interfaces: List<String> = emptyList()
    }

    @Test
    fun `public non-ignored method should be patched`() {
        val controller = ClassPatchingController.create(
            listOf(modifiedImage(TypeImage.Object("com.foo.Initializer")))
        )

        assertTrue(controller.shouldPatch(MethodInstance(ACC_PUBLIC, "foo", "()V")))
    }

    @Test
    fun `constructor should not be patched`() {
        val controller = ClassPatchingController.create(
            listOf(modifiedImage(TypeImage.Object("com.foo.Initializer")))
        )

        assertFalse(controller.shouldPatch(MethodInstance(ACC_PUBLIC, "<init>", "()V")))
        assertFalse(controller.shouldPatch(MethodInstance(ACC_PUBLIC, "<clinit>", "()V")))
    }

    @Test
    fun `non-public method should not be patched`() {
        val controller = ClassPatchingController.create(
            listOf(modifiedImage(TypeImage.Object("com.foo.Initializer")))
        )

        assertFalse(controller.shouldPatch(MethodInstance(0, "foo", "()V")))
    }

    @Test
    fun `method from the ignore list should not be patched`() {
        val controller = ClassPatchingController.create(
            listOf(modifiedImage(TypeImage.Object("com.foo.Initializer"), ignoredMethods = setOf(fooMethod)))
        )

        assertFalse(controller.shouldPatch(MethodInstance(ACC_PUBLIC, "foo", "()V")))
    }

    @Test
    fun `createMethodPatchController collects only non-ignored initializers`() {
        val controller = ClassPatchingController.create(
            listOf(
                modifiedImage(TypeImage.Object("com.foo.Initializer"), ignoredMethods = setOf(fooMethod)),
                modifiedImage(TypeImage.Object("com.foo.SecondInitializer")),
            )
        )

        val methodController = controller.createMethodPatchController(
            MethodInstance(ACC_PUBLIC, "foo", "()V")
        )

        val recording = RecordingMethodVisitor()
        methodController.injectInitializers(recording)

        // only the non-ignored initializer is embedded into the bytecode
        assertEquals(listOf("com.foo.SecondInitializer"), recording.ldcTypes.map { it.className })
    }

    @Test
    fun `program controller finds the victim by class name`() {
        val controller = ProgramPatchingController.Builder()
            .addType(modifiedImage(TypeImage.Object("com.foo.Initializer")))
            .build()

        assertTrue(controller.shouldPatch(classData("com.foo.Victim")))
    }

    @Test
    fun `program controller finds the victim also by the Kt suffix`() {
        val controller = ProgramPatchingController.Builder()
            .addType(modifiedImage(TypeImage.Object("com.foo.Initializer")))
            .build()

        assertTrue(controller.shouldPatch(classData("com.foo.VictimKt")))
    }

    @Test
    fun `program controller does not patch a mismatched class`() {
        val controller = ProgramPatchingController.Builder()
            .addType(modifiedImage(TypeImage.Object("com.foo.Initializer")))
            .build()

        assertFalse(controller.shouldPatch(classData("com.other.Class")))
    }
}
