package ru.ozon.asyncInitializer.util

import ru.ozon.asyncInitializer.library.threads.PlatformMainThread
import java.util.concurrent.CopyOnWriteArrayList

class DefaultTestPlatformMainThread(
    private val currentThreadId: Long = Thread.currentThread().id,
) : PlatformMainThread {
    private val lock = Any()
    val actions = CopyOnWriteArrayList<Runnable>()

    override val isMainThread: Boolean
        get() = currentThreadId == Thread.currentThread().id

    override fun runOnUIThread(action: Runnable) = synchronized(lock) {
        actions += action
    }

    override fun cancelRunOnUIThread(action: Runnable) = synchronized(lock) {
        actions -= action
    }
}
