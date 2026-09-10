package ru.ozon.asyncInitializer.library

import ru.ozon.asyncInitializer.library.factory.AppComponentInitializerFactory
import ru.ozon.asyncInitializer.library.platform.DefaultPlatformMainThread
import ru.ozon.asyncInitializer.library.provider.AppComponentInitializerProvider
import ru.ozon.asyncInitializer.library.store.ComponentInitializerStore
import ru.ozon.asyncInitializer.library.threads.PlatformMainThread
import ru.ozon.asyncInitializer.library.store.DefaultComponentInitializerStore

public inline fun <reified T : ComponentInitializer> getComponentInitializer(): T = getComponentInitializer(T::class.java)

public fun <T : ComponentInitializer> getComponentInitializer(initializer: Class<T>): T {
    return AppComponentInitializerProvider.Companion.getComponentInitializer(initializer)
}

public fun setupAppComponentInitializer(
    factory: AppComponentInitializerFactory,
    store: ComponentInitializerStore = DefaultComponentInitializerStore(),
    platformMainThread: PlatformMainThread = DefaultPlatformMainThread,
) {
    AppComponentInitializerProvider.Companion.reInitialize(
        factory = factory,
        store = store,
        platformMainThread = platformMainThread,
    )
}

public fun forceResetAppComponentInitializer() {
    AppComponentInitializerProvider.Companion.reset()
}
