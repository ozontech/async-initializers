package ru.ozon.asyncInitializer.library.factory

import ru.ozon.asyncInitializer.library.ComponentInitializer

/**
 * Фабрика для создания инициалайзеров
 */
public interface AppComponentInitializerFactory {

    public fun <T : ComponentInitializer> create(initializer: Class<T>): T
}
