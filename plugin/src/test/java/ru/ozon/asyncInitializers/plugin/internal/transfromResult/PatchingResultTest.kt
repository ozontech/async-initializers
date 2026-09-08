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

    /** Корректно регистрируем всё, чтобы valid-трансформация прошла без исключений. */
    private fun builderWithAllFound(): PatchingResult.Builder {
        val builder = PatchingResult.Builder(listOf(image()))
        builder.addPatchedClass(victim)
        builder.addFoundedInitializer(initializer)
        return builder
    }

    @Test
    fun `валидация проходит когда все заявленные классы и инициалайзеры найдены`() {
        builderWithAllFound().build().checkOnValidTransformation()
    }

    @Test
    fun `валидация падает когда класс-victim не найден`() {
        val builder = PatchingResult.Builder(listOf(image()))
        builder.addFoundedInitializer(initializer)

        val exception = assertFailsWith<IllegalStateException> {
            builder.build().checkOnValidTransformation()
        }

        assertTrue(exception.message.orEmpty().contains("не был модифицирован"))
    }

    @Test
    fun `валидация падает когда инициалайзер не найден`() {
        val builder = PatchingResult.Builder(listOf(image()))
        builder.addPatchedClass(victim)

        val exception = assertFailsWith<IllegalStateException> {
            builder.build().checkOnValidTransformation()
        }

        assertTrue(exception.message.orEmpty().contains("Инициалайзер"))
        assertTrue(exception.message.orEmpty().contains("не был найден"))
    }

    @Test
    fun `валидация падает когда ignore-метод не найден в классе`() {
        val builder = PatchingResult.Builder(listOf(image(ignoredMethods = setOf(fooMethod))))
        builder.addPatchedClass(victim)
        builder.addFoundedInitializer(initializer)
        // ignore-метод не регистрируем через addIgnoreMethod

        val exception = assertFailsWith<IllegalStateException> {
            builder.build().checkOnValidTransformation()
        }

        assertTrue(exception.message.orEmpty().contains("найти метод"))
        assertTrue(exception.message.orEmpty().contains("foo"))
    }

    @Test
    fun `isNeedValidate помечает victim как пропатченный класс и возвращает true`() {
        val builder = PatchingResult.Builder(listOf(image()))

        val needValidate = builder.isNeedValidate("com/foo/Victim.class")

        assertTrue(needValidate)
    }

    @Test
    fun `isNeedValidate помечает инициалайзер как найденный и возвращает false`() {
        val builder = PatchingResult.Builder(listOf(image()))

        val needValidate = builder.isNeedValidate("com/foo/Initializer.class")

        assertFalse(needValidate)
    }

    @Test
    fun `isNeedValidate не трогает посторонние классы`() {
        val builder = PatchingResult.Builder(listOf(image()))

        val needValidate = builder.isNeedValidate("com/other/Some.class")

        assertFalse(needValidate)
    }
}
