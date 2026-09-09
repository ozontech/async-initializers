package ru.ozon.asyncInitializer.library

import ru.ozon.asyncInitializer.library.cycleCallDetector.CycleCallSnapshot
import ru.ozon.asyncInitializer.library.cycleCallDetector.InitializerCycleCallDetector
import ru.ozon.asyncInitializer.library.threads.ThreadController
import java.util.UUID
import java.util.concurrent.locks.ReentrantLock

/**
 * Initializer for a specific feature
 *
 * @param runOnlyOnMainThread guarantees that initialization runs only on the MainThread
 * regardless of where it was invoked from [ComponentInitializer.initialize]
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
     * Plain initialization
     *
     * Guarantees single invocation of [runInitialize] using Double-Check locking
     *
     * Guarantees cycle detection -> (AInitializer -> BInitializer -> AInitializer)
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
     * Starts initialization when running on MainThread, or performs a blocking dispatch to MainThread
     *
     * Guarantees transferring the cycle detection logic when called -> (AInitializer -> BInitializer -> AInitializer)
     */
    private fun dispatchOnMainThreadInitialize() {
        // Optimistic path
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
