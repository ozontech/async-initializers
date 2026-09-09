package ru.ozon.asyncInitializer.library.factory

import ru.ozon.asyncInitializer.library.ComponentInitializer

/**
 * Factory for creating initializers
 */
public interface AppComponentInitializerFactory {

    public fun <T : ComponentInitializer> create(initializer: Class<T>): T
}
