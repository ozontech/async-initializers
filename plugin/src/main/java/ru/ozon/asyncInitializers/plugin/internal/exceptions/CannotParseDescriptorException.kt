package ru.ozon.asyncInitializers.plugin.internal.exceptions

internal class CannotParseDescriptorException(descriptor: String, invalidSymbol: Char): Exception(
    "Could not parse method descriptor \"$descriptor\", error while reading \"$invalidSymbol\""
)

internal class CannotParseDescriptorForMethodException(name: String, descriptor: String, cause: Throwable): Exception(
    "Could not parse descriptor for method \"$name$descriptor\"",
    cause
)
