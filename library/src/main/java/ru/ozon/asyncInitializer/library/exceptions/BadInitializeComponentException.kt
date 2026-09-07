package ru.ozon.asyncInitializer.library.exceptions

import ru.ozon.asyncInitializer.library.ComponentInitializer

/**
 * Неправильная инициализация [ComponentInitializer]
 *
 * Возможно, вы создали инстанс [ComponentInitializer], не воспользовавшись методом [ru.ozon.asyncInitializer.library.getComponentInitializer]
 */
public class BadInitializeComponentException internal constructor() : Exception(
    "Неверная инициализация (смотреть в классе ошибки)",
)
