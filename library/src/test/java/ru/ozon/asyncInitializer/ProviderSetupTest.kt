package ru.ozon.asyncInitializer

import ru.ozon.asyncInitializer.base.BaseInitializersTest
import ru.ozon.asyncInitializer.library.ComponentInitializer
import ru.ozon.asyncInitializer.library.exceptions.AlreadyInitializerAppComponentInitializerException
import ru.ozon.asyncInitializer.library.exceptions.NotOnMainThreadException
import ru.ozon.asyncInitializer.library.factory.AppComponentInitializerFactory
import ru.ozon.asyncInitializer.library.forceResetAppComponentInitializer
import ru.ozon.asyncInitializer.library.getComponentInitializer
import ru.ozon.asyncInitializer.library.setupAppComponentInitializer
import ru.ozon.asyncInitializer.library.store.ComponentInitializerStore
import ru.ozon.asyncInitializer.util.EmptyAppComponentInitializerFactory
import ru.ozon.asyncInitializer.util.FuncComponentInitializer
import ru.ozon.asyncInitializer.util.SingleInitializeComponentInitializerFactory
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import kotlin.test.assertTrue

class ProviderSetupTest : BaseInitializersTest() {


    @Test
    fun concurrentGetCreatesSingleInstance() {
        val factory = SingleInitializeComponentInitializerFactory(FuncComponentInitializer {})
        val instances = arrayOfNulls<ComponentInitializer>(8)
        val startSignal = CountDownLatch(1)

        runTest(localFactory = factory) {
            repeat(8) { index ->
                createThread {
                    instances[index] = getComponentInitializer<ComponentInitializer>()
                    startSignal.countDown()
                }
            }
            startSignal.await()
        }

        assertEquals(8, instances.size)
        assertEquals(1, instances.distinct().size)
    }


    @Test
    fun canSetupAgainAfterReset() {
        setupAppComponentInitializer(factory = EmptyAppComponentInitializerFactory)
        forceResetAppComponentInitializer()

        // Repeated setup after reset should not throw AlreadyInitializerAppComponentInitializerException
        setupAppComponentInitializer(factory = EmptyAppComponentInitializerFactory)

        clearAppComponentInitializer()
    }

    @Test
    fun setupFailsWithoutResetUntilReset() {
        setupAppComponentInitializer(factory = EmptyAppComponentInitializerFactory)

        assertFailsWith<AlreadyInitializerAppComponentInitializerException> {
            setupAppComponentInitializer(factory = EmptyAppComponentInitializerFactory)
        }

        forceResetAppComponentInitializer()
    }
}
