package ru.ozon.asyncInitializers.plugin.internal.exceptions

internal class PatchingInterfaceException(classname: String): Exception(
    "Попытка патчить интерфейс $classname"
)
