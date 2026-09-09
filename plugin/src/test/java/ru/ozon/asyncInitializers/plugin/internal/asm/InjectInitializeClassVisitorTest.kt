package ru.ozon.asyncInitializers.plugin.internal.asm

import org.junit.jupiter.api.Test
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import ru.ozon.asyncInitializers.plugin.internal.exceptions.PatchingInterfaceException
import ru.ozon.asyncInitializers.plugin.internal.image.images.MethodImage
import ru.ozon.asyncInitializers.plugin.internal.image.images.ModifiedImage
import ru.ozon.asyncInitializers.plugin.internal.image.images.TypeImage
import ru.ozon.asyncInitializers.plugin.internal.patchingController.ClassPatchingController
import ru.ozon.asyncInitializers.plugin.internal.util.WasInjectInitializerAnnotation
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

internal class InjectInitializeClassVisitorTest {

    private val victim = TypeImage.Object("com.foo.Victim")
    private val initializer = TypeImage.Object("com.foo.Initializer")
    private val marker = WasInjectInitializerAnnotation.jvmName

    private val fooMethod = MethodImage("foo", emptyList(), TypeImage.Primitive.VOID)

    private fun controller(
        modifiedImages: List<ModifiedImage> = listOf(
            ModifiedImage(victim, initializer, emptySet())
        ),
    ) = ClassPatchingController.create(modifiedImages)

    /** Applies the class visitor to a class file and returns "method -> annotation" pairs. */
    private fun patchedAnnotations(
        sourceBytes: ByteArray,
        classPatchingController: ClassPatchingController,
    ): List<Pair<String, String>> {
        val writer = ClassWriter(0)
        val classVisitor =
            InjectInitializeClassVisitor(Opcodes.ASM9, writer, classPatchingController)
        ClassReader(sourceBytes).accept(classVisitor, 0)
        val outputBytes = writer.toByteArray()

        val annotations = mutableListOf<Pair<String, String>>()
        ClassReader(outputBytes).accept(object : ClassVisitor(Opcodes.ASM9) {
            override fun visitMethod(
                access: Int,
                name: String,
                descriptor: String,
                signature: String?,
                exceptions: Array<out String?>?,
            ): MethodVisitor {
                val upstream = super.visitMethod(access, name, descriptor, signature, exceptions)
                return object : MethodVisitor(Opcodes.ASM9, upstream) {
                    override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor? {
                        annotations.add(name to descriptor)
                        return super.visitAnnotation(descriptor, visible)
                    }
                }
            }
        }, 0)
        return annotations
    }

    @Test
    fun `attempting to patch an interface throws an exception`() {
        val sourceBytes = ClassBuilderTestUtil.buildClass(
            className = "com/foo/Victim",
            access = Opcodes.ACC_PUBLIC or Opcodes.ACC_INTERFACE,
            interfaces = emptyArray(),
        )
        val writer = ClassWriter(0)
        val classVisitor = InjectInitializeClassVisitor(Opcodes.ASM9, writer, controller())

        assertFailsWith<PatchingInterfaceException> {
            ClassReader(sourceBytes).accept(classVisitor, 0)
        }
    }

    @Test
    fun `public method is marked with a marker annotation`() {
        val sourceBytes = ClassBuilderTestUtil.buildClass(
            "com/foo/Victim",
            methods = listOf(MethodSpec(Opcodes.ACC_PUBLIC, "foo", "()V")),
        )

        val annotations = patchedAnnotations(sourceBytes, controller())

        assertTrue(annotations.any { it.first == "foo" && it.second == marker })
    }

    @Test
    fun `non-public method is not marked`() {
        val sourceBytes = ClassBuilderTestUtil.buildClass(
            "com/foo/Victim",
            methods = listOf(
                MethodSpec(Opcodes.ACC_PUBLIC, "foo", "()V"),
                MethodSpec(Opcodes.ACC_PRIVATE, "bar", "()V"),
            ),
        )

        val annotations = patchedAnnotations(sourceBytes, controller())

        assertTrue(annotations.any { it.first == "foo" && it.second == marker })
        assertTrue(annotations.none { it.first == "bar" })
    }

    @Test
    fun `method from the ignore list is not marked`() {
        val sourceBytes = ClassBuilderTestUtil.buildClass(
            "com/foo/Victim",
            methods = listOf(
                MethodSpec(Opcodes.ACC_PUBLIC, "foo", "()V"),
                MethodSpec(Opcodes.ACC_PUBLIC, "baz", "()V"),
            ),
        )
        val ignoreController = controller(
            listOf(ModifiedImage(victim, initializer, setOf(fooMethod)))
        )

        val annotations = patchedAnnotations(sourceBytes, ignoreController)

        assertTrue(annotations.none { it.first == "foo" })
        assertTrue(annotations.any { it.first == "baz" && it.second == marker })
    }

    @Test
    fun `empty controller does not mark methods`() {
        val sourceBytes = ClassBuilderTestUtil.buildClass(
            "com/foo/Victim",
            methods = listOf(MethodSpec(Opcodes.ACC_PUBLIC, "foo", "()V")),
        )

        val annotations = patchedAnnotations(sourceBytes, controller(emptyList()))

        assertTrue(annotations.isEmpty())
    }
}
