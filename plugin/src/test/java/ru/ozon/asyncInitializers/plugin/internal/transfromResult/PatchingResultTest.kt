package ru.ozon.asyncInitializers.plugin.internal.transfromResult

import org.junit.jupiter.api.Test
import ru.ozon.asyncInitializers.plugin.internal.image.images.MethodImage
import ru.ozon.asyncInitializers.plugin.internal.image.images.ModifiedImage
import ru.ozon.asyncInitializers.plugin.internal.image.images.TypeImage
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class PatchingResultTest {

    private val victim = TypeImage.Object("com.foo.Victim")
    private val initializer = TypeImage.Object("com.foo.Initializer")
    private val fooMethod = MethodImage("foo", emptyList(), TypeImage.Primitive.VOID)

    private fun image(ignoredMethods: Set<MethodImage> = emptySet()) =
        ModifiedImage(victim = victim, initializer = initializer, ignoredMethods = ignoredMethods)

    /** Register everything correctly so the valid transformation runs without exceptions. */
    private fun builderWithAllFound(): PatchingResult.Builder {
        val builder = PatchingResult.Builder(listOf(image()))
        builder.addPatchedClass(victim)
        builder.addFoundedInitializer(initializer)
        return builder
    }

    @Test
    fun `validation passes when all declared classes and initializers are found`() {
        builderWithAllFound().build().checkOnValidTransformation()
    }

    @Test
    fun `validation fails when the victim class is not found`() {
        val builder = PatchingResult.Builder(listOf(image()))
        builder.addFoundedInitializer(initializer)

        val exception = assertFailsWith<IllegalStateException> {
            builder.build().checkOnValidTransformation()
        }

        assertTrue(exception.message.orEmpty().contains("was not modified"))
    }

    @Test
    fun `validation fails when the initializer is not found`() {
        val builder = PatchingResult.Builder(listOf(image()))
        builder.addPatchedClass(victim)

        val exception = assertFailsWith<IllegalStateException> {
            builder.build().checkOnValidTransformation()
        }

        assertTrue(exception.message.orEmpty().contains("Initializer"))
        assertTrue(exception.message.orEmpty().contains("was not found"))
    }

    @Test
    fun `validation fails when an ignore-method is not found in the class`() {
        val builder = PatchingResult.Builder(listOf(image(ignoredMethods = setOf(fooMethod))))
        builder.addPatchedClass(victim)
        builder.addFoundedInitializer(initializer)
        // we do not register the ignore-method via addIgnoreMethod

        val exception = assertFailsWith<IllegalStateException> {
            builder.build().checkOnValidTransformation()
        }

        assertTrue(exception.message.orEmpty().contains("find method"))
        assertTrue(exception.message.orEmpty().contains("foo"))
    }

    @Test
    fun `isNeedValidate marks the victim as patched and returns true`() {
        val builder = PatchingResult.Builder(listOf(image()))

        val needValidate = builder.isNeedValidate("com/foo/Victim.class")

        assertTrue(needValidate)
    }

    @Test
    fun `isNeedValidate marks the initializer as found and returns false`() {
        val builder = PatchingResult.Builder(listOf(image()))

        val needValidate = builder.isNeedValidate("com/foo/Initializer.class")

        assertFalse(needValidate)
    }

    @Test
    fun `isNeedValidate leaves other classes untouched`() {
        val builder = PatchingResult.Builder(listOf(image()))

        val needValidate = builder.isNeedValidate("com/other/Some.class")

        assertFalse(needValidate)
    }
}
