package ru.ozon.asyncInitializer.library.exceptions

public class MainSwitchComponentInitializerException internal constructor(
    cause: Throwable,
) : Exception(
    "Error inside component initialization after switching to MainThread",
    cause,
)
