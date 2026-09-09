package ru.ozon.asyncInitializer.library.threads

import ru.ozon.asyncInitializer.library.exceptions.AlreadyOnMainThreadException
import ru.ozon.asyncInitializer.library.exceptions.MainSwitchComponentInitializerException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.locks.ReentrantLock

/**
 * Optimized wrapper for resolving conflicts between threads
 *
 */
internal class ThreadController(
    private val mainOptimizedRunner: MainOptimizedRunner,
) {

    val isMainThread get() = mainOptimizedRunner.isMainThread

    /**
     * Function that lets you send an event to the MainThread from any thread (except MainThread)
     * Blocks the current thread until the function runs on the MainThread
     */
    fun dispatchOnMainWithBlocking(action: () -> Unit) {
        if (isMainThread) throw AlreadyOnMainThreadException()

        val future = CompletableFuture<Result<Unit>>()

        val runnable = object : Runnable {
            override fun run() {
                mainOptimizedRunner.cancelRunOnUIThread(this)

                val result = runCatching { action() }
                future.complete(result)
            }
        }

        mainOptimizedRunner.runOnUIThread(action = runnable)

        future
            .safeGet()
            .onFailure { error ->
                if (error is InterruptedException) throw error
                throw MainSwitchComponentInitializerException(error)
            }
    }

    fun <R> optimizedSynchronized(lock: ReentrantLock, action: () -> R): R {
        return when {
            !mainOptimizedRunner.isMainThread -> simpleSynchronized(lock, action)
            else -> mainOptimizedRunner.optimizedSynchronized(lock, action)
        }
    }

    private fun <R> simpleSynchronized(lock: ReentrantLock, action: () -> R): R {
        lock.lock()
        try {
            return action()
        } finally {
            lock.unlock()
            mainOptimizedRunner.forceWakeUpIfNeed()
        }
    }

    private fun CompletableFuture<Result<Unit>>.safeGet(): Result<Unit> {
        return runCatching { get().getOrThrow() }
    }
}
