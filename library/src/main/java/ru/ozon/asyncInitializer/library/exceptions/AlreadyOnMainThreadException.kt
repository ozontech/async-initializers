package ru.ozon.asyncInitializer.library.exceptions

public class AlreadyOnMainThreadException internal constructor() : Exception(
    "Вы уже находитесь на UI потоке",
)
