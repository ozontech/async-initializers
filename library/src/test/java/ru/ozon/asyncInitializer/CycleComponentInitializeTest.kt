package ru.ozon.asyncInitializer

import ru.ozon.asyncInitializer.base.BaseInitializersTest
import ru.ozon.asyncInitializer.library.ComponentInitializer
import ru.ozon.asyncInitializer.library.exceptions.CycleComponentInitializerException
import ru.ozon.asyncInitializer.library.getComponentInitializer
import ru.ozon.asyncInitializer.util.ListInitializeComponentInitializerFactory
import ru.ozon.asyncInitializer.util.SingleInitializeComponentInitializerFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class CycleComponentInitializeTest : BaseInitializersTest() {

    @Test
    fun errorIfFindRepeatComponentInitializersCall() {
        val chain = listOf(
            CycleFirstInitializer(),
            CycleSecondInitializer(),
            CycleThirdInitializer(),
            CycleFourthInitializer(),
            CycleFifthInitializer(),
        )

        var expectedThrowable: Throwable? = null

        runTest(
            localFactory = ListInitializeComponentInitializerFactory(chain),
        ) {
            try {
                getComponentInitializer<CycleFirstInitializer>().initialize()
            } catch (e: CycleComponentInitializerException) {
                expectedThrowable = e
            }
        }

        assertNotNull(expectedThrowable)
        assert(expectedThrowable is CycleComponentInitializerException)

        val exceptedMessage = CycleComponentInitializerException(
            order = chain.map { it::class.java.simpleName },
            repeatedClass = CycleFirstInitializer::class.java.simpleName,
        ).message

        assertEquals(expectedThrowable.message, exceptedMessage)
    }

    @Test
    fun errorIfFindRepeatComponentInitializersCallBetweenMainThread() {
        val chain = listOf(
            CycleFirstInitializer(),
            CycleSecondInitializer(runOnMainThread = true),
            CycleThirdInitializer(),
            CycleFourthInitializer(),
            CycleFifthInitializer(),
        )

        var expectedThrowable: Throwable? = null

        runTest(
            localFactory = ListInitializeComponentInitializerFactory(chain),
        ) {
            createThread {
                try {
                    getComponentInitializer<CycleFirstInitializer>().initialize()
                } catch (e: MainSwitchComponentInitializerException) {
                    expectedThrowable = e
                }
            }
        }

        assertNotNull(expectedThrowable)
        assert(expectedThrowable is MainSwitchComponentInitializerException)

        val expectedCause = expectedThrowable.cause

        assertNotNull(expectedCause)
        assert(expectedCause is CycleComponentInitializerException)

        val exceptedMessage = CycleComponentInitializerException(
            order = chain.map { it::class.java.simpleName },
            repeatedClass = CycleFirstInitializer::class.java.simpleName,
        ).message

        assertEquals(expectedCause.message, exceptedMessage)
    }

    @Test
    fun initializeComponentInitializerCallRepeatInsideOneComponent() {
        var value = 0
        val componentInitializer = SomeCallRepeaterComponentInitializer { value++ }

        runTest(
            localFactory = SingleInitializeComponentInitializerFactory(componentInitializer),
        ) {
            getComponentInitializer<SomeCallRepeaterComponentInitializer>().initialize()
        }

        assertEquals(value, 1)
    }

    class CycleFirstInitializer : ComponentInitializer() {
        override fun runInitialize() {
            getComponentInitializer<CycleSecondInitializer>().initialize()
        }
    }

    class CycleSecondInitializer(
        runOnMainThread: Boolean = false,
    ) : ComponentInitializer(runOnMainThread) {
        override fun runInitialize() {
            getComponentInitializer<CycleThirdInitializer>().initialize()
        }
    }

    class CycleThirdInitializer(
        runOnMainThread: Boolean = false,
    ) : ComponentInitializer(runOnMainThread) {
        override fun runInitialize() {
            getComponentInitializer<CycleFourthInitializer>().initialize()
        }
    }

    class CycleFourthInitializer(
        runOnMainThread: Boolean = false,
    ) : ComponentInitializer(runOnMainThread) {
        override fun runInitialize() {
            getComponentInitializer<CycleFifthInitializer>().initialize()
        }
    }

    class CycleFifthInitializer(
        runOnMainThread: Boolean = false,
    ) : ComponentInitializer(runOnMainThread) {
        override fun runInitialize() {
            getComponentInitializer<CycleFirstInitializer>().initialize()
        }
    }

    class SomeCallRepeaterComponentInitializer(
       private val successSignal: () -> Unit,
    ) : ComponentInitializer() {
        override fun runInitialize() {
            getComponentInitializer<SomeCallRepeaterComponentInitializer>().initialize()
            successSignal()
        }
    }
}
