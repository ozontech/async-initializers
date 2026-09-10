package ru.ozon.asyncInitializer.library.exceptions

/**
 * [ru.ozon.asyncInitializer.library.provider.AppComponentInitializerProvider] was not initialized
 *
 * You probably did not call [setupAppComponentInitializer]
 */
public class NotInitializerAppComponentInitializerException internal constructor() : Exception(
    "Access error (see the exception class)",
)

/**
 * [ru.ozon.asyncInitializer.library.provider.AppComponentInitializerProvider] was initialized again
 *
 * You probably called [setupAppComponentInitializer] again
 */
public class AlreadyInitializerAppComponentInitializerException internal constructor() : Exception(
    "Repeated initialization (see the exception class)",
)
