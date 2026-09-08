package ru.ozon.asyncInitializers.plugin.internal.exceptions

internal class CannotParseDescriptorException(descriptor: String, invalidSymbol: Char): Exception(
    "Не получилось распарсить дескриптор метода \"$descriptor\" ошибка при чтении \"$invalidSymbol\""
)

internal class CannotParseDescriptorForMethodException(name: String, descriptor: String, cause: Throwable): Exception(
    "Не получилось распарсить дескриптор для метода \"$name$descriptor\"",
    cause
)
