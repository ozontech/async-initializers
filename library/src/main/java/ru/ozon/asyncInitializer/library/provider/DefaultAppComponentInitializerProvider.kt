package ru.ozon.asyncInitializer.library.provider

import ru.ozon.asyncInitializer.library.ComponentInitializer
import ru.ozon.asyncInitializer.library.cycleCallDetector.InitializerCycleCallDetector
import ru.ozon.asyncInitializer.library.factory.AppComponentInitializerFactory
import ru.ozon.asyncInitializer.library.platform.DefaultPlatformMainThread
import ru.ozon.asyncInitializer.library.store.ComponentInitializerStore
import ru.ozon.asyncInitializer.library.threads.MainOptimizedRunner
import ru.ozon.asyncInitializer.library.threads.PlatformMainThread
import ru.ozon.asyncInitializer.library.threads.ThreadController
import ru.ozon.asyncInitializer.library.store.DefaultComponentInitializerStore

internal class DefaultAppComponentInitializerProvider(
    private val factory: AppComponentInitializerFactory,
    private val store: ComponentInitializerStore = DefaultComponentInitializerStore(),
    platformMainThread: PlatformMainThread = DefaultPlatformMainThread,
) : AppComponentInitializerProvider {
    private val writeReadLock = Any()

    private val cycleCallDetector by lazy {
        InitializerCycleCallDetector.create()
    }
    private val threadController by lazy {
        ThreadController(MainOptimizedRunner(platformMainThread))
    }

    override fun <T : ComponentInitializer> getComponentInitializer(
        initializerKey: Class<T>,
    ): T {
        return synchronized(initializerKey) {
            var initializer = synchronized(writeReadLock) { store.get(initializerKey) }
            if (initializer != null) return@synchronized initializer

            initializer = factory.create(initializerKey).provideRequiredDependencies()
            synchronized(writeReadLock) { store.put(initializerKey, initializer) }

            return@synchronized initializer
        }
    }

    private fun <T : ComponentInitializer> T.provideRequiredDependencies(): T = apply {
        provideRequiredDependencies(
            cycleCallDetector = cycleCallDetector,
            threadController = threadController,
        )
    }
}
