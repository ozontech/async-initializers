package ru.ozon.asyncInitializer.library.platform

import android.os.Handler
import android.os.Looper
import ru.ozon.asyncInitializer.library.threads.PlatformMainThread

internal object DefaultPlatformMainThread : PlatformMainThread {

    private val handler by lazy { Handler(Looper.getMainLooper()) }

    override val isMainThread: Boolean
        get() = handler.looper.thread.id == Thread.currentThread().id

    override fun runOnUIThread(action: Runnable) {
        handler.post(action)
    }

    override fun cancelRunOnUIThread(action: Runnable) {
        handler.removeCallbacks(action)
    }
}
