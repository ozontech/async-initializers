package ru.ozon.asyncInitializer

import ru.ozon.asyncInitializer.library.exceptions.NotOnMainThreadException
import ru.ozon.asyncInitializer.library.threads.MainOptimizedRunner
import ru.ozon.asyncInitializer.util.DefaultTestPlatformMainThread
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MainOptimizedRunnerTest {

    @Test
    fun optimizedSynchronizedThrowsWhenNotOnMain() {
        // isMainThread == false (current thread != declared main)
        val platform = DefaultTestPlatformMainThread(currentThreadId = Long.MAX_VALUE)
        val runner = MainOptimizedRunner(platform)
        val lock = ReentrantLock()

        assertFailsWith<NotOnMainThreadException> {
            runner.optimizedSynchronized(lock) { }
        }
    }

    @Test
    fun optimizedSynchronizedOnMainRunsActionAndReleasesLock() {
        val platform = DefaultTestPlatformMainThread()
        val runner = MainOptimizedRunner(platform)
        val lock = ReentrantLock()
        var ran = false

        runner.optimizedSynchronized(lock) {
            ran = true
            assertTrue(lock.isLocked)
        }

        assertTrue(ran)
        assertFalse(lock.isLocked)
    }

    @Test
    fun blockedMainWakesAndCompletesAfterRunOnUIThread() {
        val platform = DefaultTestPlatformMainThread()
        val runner = MainOptimizedRunner(platform)
        val lock = ReentrantLock()

        val actionRan = AtomicBoolean(false)
        val workerAcquired = CountDownLatch(1)

        // Worker holds the lock, then releases it and wakes main via runOnUIThread + forceWakeUp
        val worker = thread {
            lock.lock()
            workerAcquired.countDown()

            Thread.sleep(200)

            lock.unlock()
            runner.forceWakeUpIfNeed()
        }

        workerAcquired.await()
        // "Main" (test thread) is blocked on tryLock/await while the worker holds the lock
        runner.optimizedSynchronized(lock) {
            actionRan.set(true)
        }

        worker.join()

        assertTrue(actionRan.get())
    }
}
