package ru.ozon.asyncInitializer.library.exceptions

public class MainSwitchComponentInitializerException internal constructor(
    cause: Throwable,
) : Exception(
    "Ошибка внутри инициализации компонента после переключения на MainThread",
    cause,
)
