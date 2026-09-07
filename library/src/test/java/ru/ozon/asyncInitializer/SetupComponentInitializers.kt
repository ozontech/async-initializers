package ru.ozon.asyncInitializer

import ru.ozon.asyncInitializer.base.BaseInitializersTest
import ru.ozon.asyncInitializer.util.EmptyAppComponentInitializerFactory
import ru.ozon.asyncInitializer.util.FuncComponentInitializer
import ru.ozon.asyncInitializer.util.LazyInitializeComponentInitializerFactory
import ru.ozon.asyncInitializer.library.ComponentInitializer
import ru.ozon.asyncInitializer.library.exceptions.AlreadyInitializerAppComponentInitializerException
import ru.ozon.asyncInitializer.library.exceptions.BadInitializeComponentException
import ru.ozon.asyncInitializer.library.exceptions.MainSwitchComponentInitializerException
import ru.ozon.asyncInitializer.library.exceptions.NotInitializerAppComponentInitializerException
import ru.ozon.asyncInitializer.library.getComponentInitializer
import ru.ozon.asyncInitializer.library.setupAppComponentInitializer
import ru.ozon.asyncInitializer.util.SingleInitializeComponentInitializerFactory
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class SetupComponentInitializers : BaseInitializersTest() {

    @Test
    fun simpleSetup() {
        var value = 0

        val simpleComponentInitializer = FuncComponentInitializer {
            value++
        }

        runTest(
            localFactory = SingleInitializeComponentInitializerFactory(simpleComponentInitializer),
        ) {
            getComponentInitializer<ComponentInitializer>().initialize()
            assertEquals(value, 1)
        }
    }

    @Test
    fun forbiddenSecondSetup() {
        setupAppComponentInitializer(
            factory = EmptyAppComponentInitializerFactory,
        )

        var expectedThrowable: Throwable? = null

        try {
            setupAppComponentInitializer(factory = EmptyAppComponentInitializerFactory)
        } catch (e: AlreadyInitializerAppComponentInitializerException) {
            expectedThrowable = e
        }

        clearAppComponentInitializer()

        assertNotNull(expectedThrowable)
        assert(expectedThrowable is AlreadyInitializerAppComponentInitializerException)
    }

    @Test
    fun crashIfFactoryImplementationWithError() {
        var expectedThrowable: Throwable? = null

        try {
            getComponentInitializer<ComponentInitializer>()
        } catch (e: NotInitializerAppComponentInitializerException) {
            expectedThrowable = e
        }

        assertNotNull(expectedThrowable)
        assert(expectedThrowable is NotInitializerAppComponentInitializerException)
    }

    @Test
    fun errorInitializeComponentInitializer() {
        val simpleComponentInitializer = FuncComponentInitializer {
            error("Недостижимая точка")
        }
        var expectedThrowable: Throwable? = null

        try {
            simpleComponentInitializer.initialize()
        } catch (e: BadInitializeComponentException) {
            expectedThrowable = e
        }

        assertNotNull(expectedThrowable)
        assert(expectedThrowable is BadInitializeComponentException)
    }

    @Test
    fun successInitializeIfOneInitializeBlockInitializeFactory() {
        val countDownLatch = CountDownLatch(1)
        val counter = AtomicInteger(0)

        class FirstInitializer : ComponentInitializer() {
            init { countDownLatch.await() }
            override fun runInitialize() = Unit
        }

        class SecondInitializer : ComponentInitializer() {
            override fun runInitialize() { counter.incrementAndGet() }
        }

        runTest(
            localFactory = LazyInitializeComponentInitializerFactory(
                FirstInitializer::class to { FirstInitializer() },
                SecondInitializer::class to { SecondInitializer() },
            ),
        ) {
            createThread {
                getComponentInitializer<FirstInitializer>().initialize()
            }

            getComponentInitializer<SecondInitializer>().initialize()
            countDownLatch.countDown()
        }

        assertEquals(counter.get(), 1)
    }

    @Test
    fun canReInitializeAfterError() {
        class TestError : Exception()

        var counter = 0
        val componentInitializer = FuncComponentInitializer {
            if (counter++ == 0) throw TestError()
        }

        val factory = SingleInitializeComponentInitializerFactory(componentInitializer)

        runTest(localFactory = factory) {
            try {
                getComponentInitializer<ComponentInitializer>().initialize()
            } catch (_: TestError) {
                getComponentInitializer<ComponentInitializer>().initialize()
            }
        }

        assertEquals(counter, 2)
    }

    @Test
    fun whenErrorWasOnMainThreadGetInformationError() {
        class TestError : Exception()
        val expectedThrowable = AtomicReference<Throwable?>(null)

        val componentInitializer = FuncComponentInitializer(runOnlyOnMainThread = true) {
            throw TestError()
        }

        runTest(
            localFactory = SingleInitializeComponentInitializerFactory(componentInitializer),
        ) {
            createThread {
                try {
                    getComponentInitializer<ComponentInitializer>().initialize()
                } catch (e: MainSwitchComponentInitializerException) {
                    expectedThrowable.set(e)
                }
            }
        }

        val finalThrowable = expectedThrowable.get()
        assertNotNull(finalThrowable)
        assert(finalThrowable is MainSwitchComponentInitializerException)
    }

    @Test
    fun mainComponentInitializerShouldInitializeOnMainThread() {
        var value = 0
        val componentInitializer = FuncComponentInitializer(runOnlyOnMainThread = true) { value++ }

        runTest(
            localFactory = SingleInitializeComponentInitializerFactory(componentInitializer),
        ) {
            createThread {
                getComponentInitializer<ComponentInitializer>().initialize()
            }
        }

        assertEquals(value, 1)
    }
}
