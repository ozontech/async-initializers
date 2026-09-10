package ru.ozon.asyncInitializer.library.threads

import ru.ozon.asyncInitializer.library.exceptions.NotOnMainThreadException
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Subclass of [PlatformMainThread] responsible for optimization
 * when running initializers on the MainThread
 *
 * This class solves the DeadLock problem when the n-th thread waits for a [runOnUIThread] block, while the MainThread,
 * doing its own initialization, cannot access a [ru.ozon.asyncInitializer.library.ComponentInitializer] instance,
 * because it is held by the n-th thread
 */
internal class MainOptimizedRunner(
    private val platformMainThread: PlatformMainThread,
) : PlatformMainThread by platformMainThread {
    private val monitorLock = ReentrantLock()
    private val monitorCondition = monitorLock.newCondition()
    private val sentActionOnMain = hashSetOf<Runnable>()

    override fun runOnUIThread(action: Runnable) {
        monitorLock.withLock {
            sentActionOnMain += action
            monitorCondition.signal()
        }

        platformMainThread.runOnUIThread(action)
    }

    override fun cancelRunOnUIThread(action: Runnable) {
        monitorLock.withLock {
            sentActionOnMain.remove(action)
        }
        platformMainThread.cancelRunOnUIThread(action)
    }

    /**
     * Optimized synchronization for MainThread has a mechanism that helps avoid DeadLock
     * for the MainThread
     */
    fun <R> optimizedSynchronized(lock: ReentrantLock, action: () -> R): R {
        if (!isMainThread) throw NotOnMainThreadException()

        var isMainThreadLocked = false
        monitorLock.withLock {
            while (!isMainThreadLocked && !lock.tryLock()) {
                sentActionOnMain.toList().forEach { runnable -> runnable.run() }
                sentActionOnMain.clear()

                if (lock.tryLock()) {
                    isMainThreadLocked = true
                } else {
                    monitorCondition.await()
                }
            }
        }

        try {
            return action()
        } finally {
            lock.unlock()
        }
    }

    fun forceWakeUpIfNeed() = monitorLock.withLock {
        monitorCondition.signal()
    }
}
