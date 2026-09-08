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
    fun `публичный неигнорируемый метод должен патчиться`() {
        val controller = ClassPatchingController.create(
            listOf(modifiedImage(TypeImage.Object("com.foo.Initializer")))
        )

        assertTrue(controller.shouldPatch(MethodInstance(ACC_PUBLIC, "foo", "()V")))
    }

    @Test
    fun `конструктор не должен патчиться`() {
        val controller = ClassPatchingController.create(
            listOf(modifiedImage(TypeImage.Object("com.foo.Initializer")))
        )

        assertFalse(controller.shouldPatch(MethodInstance(ACC_PUBLIC, "<init>", "()V")))
        assertFalse(controller.shouldPatch(MethodInstance(ACC_PUBLIC, "<clinit>", "()V")))
    }

    @Test
    fun `непубличный метод не должен патчиться`() {
        val controller = ClassPatchingController.create(
            listOf(modifiedImage(TypeImage.Object("com.foo.Initializer")))
        )

        assertFalse(controller.shouldPatch(MethodInstance(0, "foo", "()V")))
    }

    @Test
    fun `метод из списка ignore не должен патчиться`() {
        val controller = ClassPatchingController.create(
            listOf(modifiedImage(TypeImage.Object("com.foo.Initializer"), ignoredMethods = setOf(fooMethod)))
        )

        assertFalse(controller.shouldPatch(MethodInstance(ACC_PUBLIC, "foo", "()V")))
    }

    @Test
    fun `createMethodPatchController собирает только неигнорируемые инициалайзеры`() {
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

        // в байткод встраивается только неигнорируемый инициалайзер
        assertEquals(listOf("com.foo.SecondInitializer"), recording.ldcTypes.map { it.className })
    }

    @Test
    fun `program контролятор находит victim по имени класса`() {
        val controller = ProgramPatchingController.Builder()
            .addType(modifiedImage(TypeImage.Object("com.foo.Initializer")))
            .build()

        assertTrue(controller.shouldPatch(classData("com.foo.Victim")))
    }

    @Test
    fun `program контролятор находит victim и по суффиксу Kt`() {
        val controller = ProgramPatchingController.Builder()
            .addType(modifiedImage(TypeImage.Object("com.foo.Initializer")))
            .build()

        assertTrue(controller.shouldPatch(classData("com.foo.VictimKt")))
    }

    @Test
    fun `program контролятор не патчит несовпадающий класс`() {
        val controller = ProgramPatchingController.Builder()
            .addType(modifiedImage(TypeImage.Object("com.foo.Initializer")))
            .build()

        assertFalse(controller.shouldPatch(classData("com.other.Class")))
    }
}
