package ru.ozon.asyncInitializer.library.exceptions

import ru.ozon.asyncInitializer.library.ComponentInitializer

/**
 * Incorrect initialization of [ComponentInitializer]
 *
 * You may have created a [ComponentInitializer] instance without using [getComponentInitializer]
 */
public class BadInitializeComponentException internal constructor() : Exception(
    "Incorrect initialization (see the exception class)",
)
