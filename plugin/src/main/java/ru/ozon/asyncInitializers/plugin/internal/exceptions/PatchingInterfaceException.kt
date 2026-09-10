package ru.ozon.asyncInitializers.plugin.internal.exceptions

internal class PatchingInterfaceException(classname: String): Exception(
    "Attempting to patch interface $classname"
)
