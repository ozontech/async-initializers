package ru.ozon.asyncInitializer

import ru.ozon.asyncInitializer.base.BaseInitializersTest
import ru.ozon.asyncInitializer.library.ComponentInitializer
import ru.ozon.asyncInitializer.util.DynamicLazyInitializeComponentInitializerFactory
import ru.ozon.asyncInitializer.library.getComponentInitializer
import java.util.concurrent.CountDownLatch
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ChainCallComponentInitializers : BaseInitializersTest() {

    @Test
    fun simpleChainInitializeCallComponentInitializers() {

        val factory = DynamicLazyInitializeComponentInitializerFactory()
        runOrderTest(localFactory = factory) {
            val chain = listOf(
                ChainFirstInitializer::class to { ChainFirstInitializer { expected(1) } },
                ChainSecondInitializer::class to { ChainSecondInitializer { expected(2) } },
                ChainThirdInitializer::class to { ChainThirdInitializer { expected(3) } },
                ChainFourthInitializer::class to { ChainFourthInitializer { expected(4) } },
                ChainFifthInitializer::class to { ChainFifthInitializer { expected(5) } },
            )

            factory.modify(chain)

            getComponentInitializer<ChainFirstInitializer>().initialize()
            expectedLast(5)
        }
    }

    @Test
    fun asyncChainInitializerCallComponentInitializers() {
        val factory = DynamicLazyInitializeComponentInitializerFactory()
        runOrderTest(localFactory = factory) {
            val chain = listOf(
                ChainFirstInitializer::class to { ChainFirstInitializer { freeOrderSignal() } },
                ChainSecondInitializer::class to { ChainSecondInitializer { freeOrderSignal() } },
                ChainThirdInitializer::class to { ChainThirdInitializer { freeOrderSignal() } },
                ChainFourthInitializer::class to { ChainFourthInitializer { freeOrderSignal() } },
                ChainFifthInitializer::class to { ChainFifthInitializer { freeOrderSignal() } },
            )

            factory.modify(chain)

            createThread {
                getComponentInitializer<ChainThirdInitializer>().initialize()
            }

            getComponentInitializer<ChainFirstInitializer>().initialize()
            expectedLast(5)
        }
    }

    @Test
    fun simpleChainInitializeCallComponentInitializersWithMainDispatch() {
        val factory = DynamicLazyInitializeComponentInitializerFactory()
        runOrderTest(localFactory = factory) {
            val chain = listOf(
                ChainFirstInitializer::class to {
                    ChainFirstInitializer {
                        assertFalse { isMainTread }
                        expected(1)
                    }
                },
                ChainSecondInitializer::class to {
                    ChainSecondInitializer {
                        assertFalse { isMainTread }
                        expected(2)
                    }
                },
                ChainThirdInitializer::class to {
                    ChainThirdInitializer(runOnMainThread = true) {
                        assertTrue { isMainTread }
                        expected(3)
                    }
                },
                ChainFourthInitializer::class to {
                    ChainFourthInitializer {
                        assertTrue { isMainTread }
                        expected(4)
                    }
                },
                ChainFifthInitializer::class to {
                    ChainFifthInitializer {
                        assertTrue { isMainTread }
                        expected(5)
                    }
                },
            )

            factory.modify(chain)

            createThread {
                getComponentInitializer<ChainFirstInitializer>().initialize()
            }
            awaitFinishAll()
            expectedLast(5)
        }
    }

    @Test
    fun asyncChainInitializerCallComponentInitializersWithMainDispatch() {
        val factory = DynamicLazyInitializeComponentInitializerFactory()

        runOrderTest(localFactory = factory) {
            val chain = listOf(
                ChainFirstInitializer::class to { ChainFirstInitializer() },
                AlternativeChainFirstInitializer::class to { AlternativeChainFirstInitializer() },
                ChainSecondInitializer::class to { ChainSecondInitializer() },
                AlternativeChainSecondInitializer::class to { AlternativeChainSecondInitializer() },
                ChainThirdInitializer::class to { ChainThirdInitializer { expected(1) } },
                ChainFourthInitializer::class to { ChainFourthInitializer { expected(2) } },
                ChainFifthInitializer::class to { ChainFifthInitializer { expected(3) } },
            )

            factory.modify(chain)

            val countDownLatch = CountDownLatch(2)
            createThread {
                getComponentInitializer<ChainFirstInitializer>().initialize()
                freeOrderSignal()
                countDownLatch.countDown()
            }

            createThread {
                getComponentInitializer<AlternativeChainFirstInitializer>().initialize()
                freeOrderSignal()
                countDownLatch.countDown()
            }

            countDownLatch.await()
            expectedLast(5)
        }
    }

    @Test
    fun waitCpuInitializerForMainThread() {
        val factory = DynamicLazyInitializeComponentInitializerFactory()
        runOrderTest(localFactory = factory) {
            val countDownLatch = CountDownLatch(1)

            val chain = listOf(
                ChainFirstInitializer::class to { ChainFirstInitializer { expected(1) } },
                ChainSecondInitializer::class to {
                    ChainSecondInitializer {
                        expected(2)
                        countDownLatch.countDown()
                    }
                },
                ChainThirdInitializer::class to {
                    ChainThirdInitializer {
                        countDownLatch.await()
                        expected(3)
                    }
                },
                ChainFourthInitializer::class to { ChainFourthInitializer { expected(4) } },
                ChainFifthInitializer::class to { ChainFifthInitializer { expected(5) } },
            )

            factory.modify(chain)

            createThread {
                getComponentInitializer<ChainThirdInitializer>().initialize()
            }

            getComponentInitializer<ChainFirstInitializer>().initialize()
            expectedLast(5)
        }
    }

    @Test
    fun mainThreadRunBlockerComponentInitializersWhenItWait() {
        val factory = DynamicLazyInitializeComponentInitializerFactory()
        runOrderTest(localFactory = factory) {
            val cpuCountDownLatch = CountDownLatch(1)

            val chain = listOf(
                ChainFirstInitializer::class to {
                    ChainFirstInitializer {
                        assertTrue { true }
                        expected(1)
                    }
                },
                ChainSecondInitializer::class to {
                    ChainSecondInitializer {
                        assertTrue { true }
                        expected(2)
                        cpuCountDownLatch.countDown()
                    }
                },
                ChainThirdInitializer::class to {
                    ChainThirdInitializer {
                        assertFalse { isMainTread }
                        cpuCountDownLatch.await()
                        expected(3)
                    }
                },
                ChainFourthInitializer::class to {
                    ChainFourthInitializer(runOnMainThread = true) {
                        assertTrue { isMainTread }
                        expected(4)
                    }
                },
                ChainFifthInitializer::class to {
                    ChainFifthInitializer {
                        assertTrue { isMainTread }
                        expected(5)
                    }
                },
            )

            factory.modify(chain)

            createThread {
                getComponentInitializer<ChainThirdInitializer>().initialize()
            }

            getComponentInitializer<ChainFirstInitializer>().initialize()
            expectedLast(5)
        }
    }

    class ChainFirstInitializer(
        runOnMainThread: Boolean = false,
        private val block: () -> Unit = {},
    ) : ComponentInitializer(runOnMainThread) {
        override fun runInitialize() {
            block()
            getComponentInitializer<ChainSecondInitializer>().initialize()
        }
    }

    class ChainSecondInitializer(
        runOnMainThread: Boolean = false,
        private val block: () -> Unit = {},
    ) : ComponentInitializer(runOnMainThread) {
        override fun runInitialize() {
            block()
            getComponentInitializer<ChainThirdInitializer>().initialize()
        }
    }

    class AlternativeChainFirstInitializer(
        runOnMainThread: Boolean = false,
        private val block: () -> Unit = {},
    ) : ComponentInitializer(runOnMainThread) {
        override fun runInitialize() {
            block()
            getComponentInitializer<AlternativeChainSecondInitializer>().initialize()
        }
    }

    class AlternativeChainSecondInitializer(
        runOnMainThread: Boolean = false,
        private val block: () -> Unit = {},
    ) : ComponentInitializer(runOnMainThread) {
        override fun runInitialize() {
            block()
            getComponentInitializer<ChainThirdInitializer>().initialize()
        }
    }

    class ChainThirdInitializer(
        runOnMainThread: Boolean = false,
        private val block: () -> Unit = {},
    ) : ComponentInitializer(runOnMainThread) {
        override fun runInitialize() {
            block()
            getComponentInitializer<ChainFourthInitializer>().initialize()
        }
    }

    class ChainFourthInitializer(
        runOnMainThread: Boolean = false,
        private val block: () -> Unit = {},
    ) : ComponentInitializer(runOnMainThread) {
        override fun runInitialize() {
            block()
            getComponentInitializer<ChainFifthInitializer>().initialize()
        }
    }

    class ChainFifthInitializer(
        runOnMainThread: Boolean = false,
        private val block: () -> Unit = {},
    ) : ComponentInitializer(runOnMainThread) {
        override fun runInitialize() {
            block()
        }
    }
}
