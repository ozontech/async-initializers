package ru.ozon.asyncInitializer.base

import ru.ozon.asyncInitializer.library.factory.AppComponentInitializerFactory
import ru.ozon.asyncInitializer.library.forceResetAppComponentInitializer
import ru.ozon.asyncInitializer.library.setupAppComponentInitializer
import ru.ozon.asyncInitializer.library.store.ComponentInitializerStore
import ru.ozon.asyncInitializer.library.store.DefaultComponentInitializerStore
import ru.ozon.asyncInitializer.util.DefaultTestPlatformMainThread
import ru.ozon.asyncInitializer.util.EmptyAppComponentInitializerFactory
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.concurrent.thread
import kotlin.test.assertEquals

abstract class BaseInitializersTest(
    private val globalFactory: AppComponentInitializerFactory = EmptyAppComponentInitializerFactory,
    private val globalStore: ComponentInitializerStore = DefaultComponentInitializerStore(),
) {

    fun runTest(
        localFactory: AppComponentInitializerFactory? = null,
        localStore: ComponentInitializerStore? = null,
        block: TestScope.() -> Unit,
    ) {
        val factory = localFactory ?: globalFactory
        val store = localStore ?: globalStore
        val platformMainThread = DefaultTestPlatformMainThread()

        try {
            setupAppComponentInitializer(
                factory = factory,
                store = store,
                platformMainThread = platformMainThread,
            )

            val scope = TestScopeImpl(platformMainThread)
            block(scope)
            scope.awaitFinishAll()
        } finally {
            clearAppComponentInitializer()
        }
    }

    fun runOrderTest(
        localFactory: AppComponentInitializerFactory? = null,
        localStore: ComponentInitializerStore? = null,
        block: OrderTestScope.() -> Unit,
    ) = runTest(
        localFactory = localFactory,
        localStore = localStore,
    ) {
        val orderTestScope = OrderTestScopeImpl(this)
        block(orderTestScope)
    }

    interface OrderTestScope : TestScope {

        fun freeOrderSignal()
        fun expected(index: Int)

        fun expectedLast(index: Int)
    }

    interface TestScope {

        val isMainTread: Boolean
        fun createThread(block: () -> Unit)

        fun awaitFinishAll()
    }

    private class TestScopeImpl(
        private val platformMainThread: DefaultTestPlatformMainThread,
    ) : TestScope {
        private val threads = CopyOnWriteArrayList<Thread>()

        override val isMainTread: Boolean
            get() = platformMainThread.isMainThread

        override fun createThread(block: () -> Unit) {
            val threadRef = thread(block = block)
            threads.add(threadRef)
        }

        override fun awaitFinishAll() {
            var isAnotherThreadAllIInitialize = false
            while (!isAnotherThreadAllIInitialize) {
                platformMainThread.actions.forEach { action -> action.run() }
                isAnotherThreadAllIInitialize = threads.all { thread -> !thread.isAlive }
            }
        }
    }

    private class OrderTestScopeImpl(
        testScope: TestScope,
    ) : OrderTestScope, TestScope by testScope {

        private val lock = Any()
        private var orderNumber = 0

        override fun freeOrderSignal(): Unit = synchronized(lock) {
            orderNumber++
        }

        override fun expected(index: Int) = synchronized(lock) {
            assertEquals(orderNumber + 1, index)
            orderNumber = index
        }

        override fun expectedLast(index: Int) = synchronized(lock) {
            assertEquals(orderNumber, index)
        }
    }

    protected fun clearAppComponentInitializer() {
        forceResetAppComponentInitializer()
    }
}
