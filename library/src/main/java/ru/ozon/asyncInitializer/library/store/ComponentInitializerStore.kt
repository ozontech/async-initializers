package ru.ozon.asyncInitializer.library.store

import ru.ozon.asyncInitializer.library.ComponentInitializer

public interface ComponentInitializerStore {
    public fun <T : ComponentInitializer> get(initializerKey: Class<T>): T?
    public fun <T : ComponentInitializer> put(initializerKey: Class<T>, value: T)
}
