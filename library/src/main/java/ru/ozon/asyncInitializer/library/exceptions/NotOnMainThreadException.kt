package ru.ozon.asyncInitializer.library.exceptions

public class NotOnMainThreadException internal constructor() : Exception(
    "Ожидалось, что вы будете на UI потоке",
)
