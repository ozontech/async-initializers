package ru.ozon.asyncInitializer.library.threads

import ru.ozon.asyncInitializer.library.exceptions.NotOnMainThreadException
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Наследник [PlatformMainThread], который берет на себя ответственность за оптимизацию
 * при выполнении инициалайзеров на MainThread
 *
 * Данный класс позволяет решить проблему DeadLock, когда n-ый поток ожидает выполнения [runOnUIThread] блока, и в то же время MainThread,
 * проводя инициализацию, не может обратиться к экземпляру [ru.ozon.asyncInitializer.library.ComponentInitializer],
 * поскольку тот занят n-ым потоком
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
     * Оптимизированная синхронизация для MainThread имеет механизм, помогающий избавиться от DeadLock
     * для MainThread
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
