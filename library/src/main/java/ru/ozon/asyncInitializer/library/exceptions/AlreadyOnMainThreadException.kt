package ru.ozon.asyncInitializer.library.exceptions

public class AlreadyOnMainThreadException internal constructor() : Exception(
    "You are already on the UI thread",
)
