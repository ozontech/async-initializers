package ru.ozon.asyncInitializer.library.threads

public interface PlatformMainThread {

    public val isMainThread: Boolean

    public fun runOnUIThread(action: Runnable)

    public fun cancelRunOnUIThread(action: Runnable)
}
