package ru.ozon.asyncInitializer.library.provider

import ru.ozon.asyncInitializer.library.ComponentInitializer
import ru.ozon.asyncInitializer.library.exceptions.AlreadyInitializerAppComponentInitializerException
import ru.ozon.asyncInitializer.library.exceptions.NotInitializerAppComponentInitializerException
import ru.ozon.asyncInitializer.library.factory.AppComponentInitializerFactory
import ru.ozon.asyncInitializer.library.platform.DefaultPlatformMainThread
import ru.ozon.asyncInitializer.library.store.ComponentInitializerStore
import ru.ozon.asyncInitializer.library.threads.PlatformMainThread
import ru.ozon.asyncInitializer.library.store.DefaultComponentInitializerStore

/**
 * Реализация данного класса должна являться точкой входа
 * для получения инициалайзера вместе со всеми его зависимостями
 */
public interface AppComponentInitializerProvider {

    public fun <T : ComponentInitializer> getComponentInitializer(initializer: Class<T>): T

    public companion object : AppComponentInitializerProvider {
        @Volatile
        private var _instance: AppComponentInitializerProvider? = null
        private val instance: AppComponentInitializerProvider
            get() = _instance ?: throw NotInitializerAppComponentInitializerException()

        private val lock = Any()

        internal fun reInitialize(
            factory: AppComponentInitializerFactory,
            store: ComponentInitializerStore = DefaultComponentInitializerStore(),
            platformMainThread: PlatformMainThread = DefaultPlatformMainThread,
        ) {
            if (_instance != null) throw AlreadyInitializerAppComponentInitializerException()

            synchronized(lock) {
                if (_instance != null) throw AlreadyInitializerAppComponentInitializerException()

                _instance = DefaultAppComponentInitializerProvider(
                    factory = factory,
                    store = store,
                    platformMainThread = platformMainThread,
                )
            }
        }

        internal fun reset() {
            _instance = null
        }

        override fun <T : ComponentInitializer> getComponentInitializer(
            initializer: Class<T>,
        ): T = instance.getComponentInitializer(initializer)
    }
}
