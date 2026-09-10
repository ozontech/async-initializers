package ru.ozon.asyncInitializer.library.store

import ru.ozon.asyncInitializer.library.ComponentInitializer

internal class DefaultComponentInitializerStore : ComponentInitializerStore {
    private val cache = mutableMapOf<Class<out ComponentInitializer>, ComponentInitializer>()

    override fun <T : ComponentInitializer> get(
        initializerKey: Class<T>,
    ): T? {
        val value = cache[initializerKey] ?: return null
        @Suppress("UNCHECKED_CAST")
        return value as T
    }

    override fun <T : ComponentInitializer> put(
        initializerKey: Class<T>,
        value: T,
    ) {
        cache[initializerKey] = value
    }
}
