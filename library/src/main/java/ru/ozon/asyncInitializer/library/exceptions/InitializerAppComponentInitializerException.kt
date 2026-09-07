package ru.ozon.asyncInitializer.library.exceptions

/**
 * Не был инициализирован [ru.ozon.asyncInitializer.library.provider.AppComponentInitializerProvider]
 *
 * Возможно, вы не вызвали [ru.ozon.asyncInitializer.library.setupAppComponentInitializer]
 */
public class NotInitializerAppComponentInitializerException internal constructor() : Exception(
    "Ошибка обращения (смотреть в классе ошибки)",
)

/**
 * Повторная инициализация [ru.ozon.asyncInitializer.library.provider.AppComponentInitializerProvider]
 *
 * Возможно, вы повторно вызвали [ru.ozon.asyncInitializer.library.setupAppComponentInitializer]
 */
public class AlreadyInitializerAppComponentInitializerException internal constructor() : Exception(
    "Повторная инициализация (смотреть в классе ошибки)",
)
