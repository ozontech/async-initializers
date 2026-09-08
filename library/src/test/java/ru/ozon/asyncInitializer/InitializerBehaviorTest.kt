package ru.ozon.asyncInitializer

import ru.ozon.asyncInitializer.base.BaseInitializersTest
import ru.ozon.asyncInitializer.library.ComponentInitializer
import ru.ozon.asyncInitializer.library.getComponentInitializer
import ru.ozon.asyncInitializer.util.DynamicLazyInitializeComponentInitializerFactory
import ru.ozon.asyncInitializer.util.FuncComponentInitializer
import ru.ozon.asyncInitializer.util.SingleInitializeComponentInitializerFactory
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class InitializerBehaviorTest : BaseInitializersTest() {


    @Test
    fun mainThreadInitializerRunsOnceAcrossThreads() {
        val counter = AtomicInteger(0)
        val initializer = FuncComponentInitializer(runOnlyOnMainThread = true) { counter.incrementAndGet() }

        runTest(localFactory = SingleInitializeComponentInitializerFactory(initializer)) {
            createThread { getComponentInitializer<ComponentInitializer>().initialize() }
            createThread { getComponentInitializer<ComponentInitializer>().initialize() }
        }

        assertEquals(1, counter.get())
    }

    @Test
    fun alreadyInitializedDoesNotDispatchAgain() {
        val counter = AtomicInteger(0)
        val initializer = FuncComponentInitializer(runOnlyOnMainThread = true) { counter.incrementAndGet() }

        runTest(localFactory = SingleInitializeComponentInitializerFactory(initializer)) {
            createThread { getComponentInitializer<ComponentInitializer>().initialize() }
            awaitFinishAll()

            // Повторный вызов с нового потока попадает в оптимистичную ветку `if (wasInitialized) return`
            createThread { getComponentInitializer<ComponentInitializer>().initialize() }
            awaitFinishAll()
        }

        assertEquals(1, counter.get())
    }

}
