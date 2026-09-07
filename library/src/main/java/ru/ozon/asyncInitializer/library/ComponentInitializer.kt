package ru.ozon.asyncInitializer.library

import ru.ozon.asyncInitializer.library.cycleCallDetector.CycleCallSnapshot
import ru.ozon.asyncInitializer.library.cycleCallDetector.InitializerCycleCallDetector
import ru.ozon.asyncInitializer.library.threads.ThreadController
import java.util.UUID
import java.util.concurrent.locks.ReentrantLock

/**
 * Инициалайзер для конкретной фичи
 *
 * @param runOnlyOnMainThread гарантирует, что инициализация будет происходить только на MainThread
 * вне зависимости откуда тот был вызван [ru.ozon.hire.appcomponentinitializers.library.ComponentInitializer.initialize]
 */
public abstract class ComponentInitializer(
    public val runOnlyOnMainThread: Boolean = false,
) {
    private var cycleCallDetector: InitializerCycleCallDetector by lateSingleInitialize()
    private var threadController: ThreadController by lateSingleInitialize()

    internal val uniqueId = UUID.randomUUID().toString()

    internal fun provideRequiredDependencies(
        cycleCallDetector: InitializerCycleCallDetector,
        threadController: ThreadController,
    ) {
        this.cycleCallDetector = cycleCallDetector
        this.threadController = threadController
    }

    @Volatile
    private var wasInitialized: Boolean = false
    private val lock = ReentrantLock()

    public fun initialize() {
        when {
            runOnlyOnMainThread -> dispatchOnMainThreadInitialize()
            else -> defaultInitialize()
        }
    }

    protected abstract fun runInitialize()

    /**
     * Простая инициализация
     *
     * Гарантирует однократный вызов метода [runInitialize], используя механизм DoubleCheck
     *
     * Гарантирует обнаружение циклов -> (AInitializer -> BInitializer -> AInitializer)
     */
    private fun defaultInitialize() {
        if (cycleCallDetector.isRepeatCall(this)) return

        cycleCallDetector.detectOnCycle(this) {
            if (wasInitialized) {
                return@detectOnCycle
            }

            threadController.optimizedSynchronized(lock) {
                if (wasInitialized) {
                    return@optimizedSynchronized
                }

                runInitialize()
                wasInitialized = true
            }
        }
    }

    /**
     * Старт инициализации при запуске на MainThread или блокирующий диспатчинг на MainThread
     *
     * Гарантирует перенос логики обнаружения циклов при вызове -> (AInitializer -> BInitializer -> AInitializer)
     */
    private fun dispatchOnMainThreadInitialize() {
        // Оптимистичный путь
        if (wasInitialized) return

        if (threadController.isMainThread) {
            defaultInitialize()
            return
        }

        val snapshot = cycleCallDetector.takeCycleCallSnapshot()
        threadController.dispatchOnMainWithBlocking {
            continueOnMainThread(snapshot)
        }
    }

    private fun continueOnMainThread(
        snapshot: CycleCallSnapshot,
    ) = cycleCallDetector.continueDetectFrom(snapshot) {
        defaultInitialize()
    }
}
