package ru.ozon.asyncInitializer.app.presentation.initializers

import android.os.Handler
import android.os.HandlerThread
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.android.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withContext
import ru.ozon.asyncInitializer.app.domain.models.Initializer
import ru.ozon.asyncInitializer.library.forceResetAppComponentInitializer
import ru.ozon.asyncInitializer.library.setupAppComponentInitializer
import ru.ozon.asyncInitializer.library.threads.PlatformMainThread

import java.util.concurrent.atomic.AtomicBoolean

internal class InitializerRunner {

    private val isRunning = AtomicBoolean(false)

    fun run(
        testCount: Int,
        initializers: List<Initializer>
    ): Flow<RunStatus> {
        if (!isRunning.compareAndSet(false, true)) return flowOf(RunStatus.AlreadyRunning)

        val platformMainThread = createPlatformMainThread()
        val store = DemoComponentInitializerStore()

        return channelFlow<RunStatus> {
            setupAppComponentInitializer(
                factory = DemoFactoryAppComponentInitializerFactory(initializers),
                store = store,
                platformMainThread = platformMainThread
            )

            val syncSpendTime = LongArray(testCount)
            val asyncSpendTime = LongArray(testCount)

            send(RunStatus.Start)

            //run sync
            repeat(testCount) { index ->
                store.clear()

                send(
                    RunStatus.UpdateTestStatus.Running(
                        isSync = true,
                        index = index
                    )
                )

                val spendTime = runSyncTest(
                    dispatcher =  platformMainThread.handler.asCoroutineDispatcher(),
                    initializers = initializers,
                )

                syncSpendTime[index] = spendTime

                send(
                    RunStatus.UpdateTestStatus.Finish(
                        isSync = true,
                        index = index,
                        spendTime = spendTime
                    )
                )
            }

            // run async
            repeat(testCount) { index ->
                store.clear()

                send(
                    RunStatus.UpdateTestStatus.Running(
                        isSync = false,
                        index = index
                    )
                )

                val spendTime = runAsyncTest(
                    dispatcher =  Dispatchers.IO,
                    initializers = initializers,
                )

                asyncSpendTime[index] = spendTime

                send(
                    RunStatus.UpdateTestStatus.Finish(
                        isSync = false,
                        index = index,
                        spendTime = spendTime
                    )
                )
            }


            send(
                RunStatus.Complete(
                    syncMedianMs = medianOf(syncSpendTime),
                    asyncMedianMs = medianOf(asyncSpendTime)
                )
            )

        }
            .onCompletion {
                forceResetAppComponentInitializer()
                platformMainThread.reset()
                store.clear()
                isRunning.set(false)
            }
    }

    private suspend fun runSyncTest(
        dispatcher: CoroutineDispatcher,
        initializers: List<Initializer>,
    ): Long = withContext(dispatcher) {
        val startTime = System.currentTimeMillis()
        initializers.forEach { initializer ->
            runInterruptible {  getDemoComponentInitializer(initializer).initialize() }
        }
        return@withContext  System.currentTimeMillis() - startTime
    }

    private suspend fun runAsyncTest(
        dispatcher: CoroutineDispatcher,
        initializers: List<Initializer>,
    ): Long = withContext(dispatcher) {
        val startTime = System.currentTimeMillis()
        initializers.map { initializer ->

            async { runInterruptible {  getDemoComponentInitializer(initializer).initialize() } }
        }
            .awaitAll()

        return@withContext System.currentTimeMillis() - startTime
    }

    private fun medianOf(values: LongArray): Long {
        val sorted = values.sorted()
        val mid = sorted.size / 2
        return if (sorted.size % 2 == 0) {
            (sorted[mid - 1] + sorted[mid]) / 2
        } else {
            sorted[mid]
        }
    }

    private fun createPlatformMainThread() = object : PlatformMainThread {
        private val thread = HandlerThread("TestMainThread").apply { start() }
        val handler = Handler(thread.looper)

        override val isMainThread: Boolean
            get() = Thread.currentThread().id == thread.id

        override fun runOnUIThread(action: Runnable) {
            handler.post(action)
        }

        override fun cancelRunOnUIThread(action: Runnable) {
            handler.removeCallbacks(action)
        }

        fun reset() {
            thread.quit()
        }

    }


    sealed interface RunStatus {
        data object AlreadyRunning: RunStatus

        data object Start: RunStatus

        sealed interface UpdateTestStatus: RunStatus  {
            val isSync: Boolean
            val index: Int

            data class Running(
                override val isSync: Boolean,
                override val index: Int
            ): UpdateTestStatus

            data class Finish(
                override val isSync: Boolean,
                override val index: Int,
                val spendTime: Long
            ): UpdateTestStatus
        }

        data class Complete(
            val syncMedianMs: Long,
            val asyncMedianMs: Long,
        ): RunStatus

    }

}
