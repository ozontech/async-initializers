package ru.ozon.asyncInitializer.library.exceptions

public class NotOnMainThreadException internal constructor() : Exception(
    "Expected to be on the UI thread",
)
