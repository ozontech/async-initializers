package ru.ozon.asyncInitializers.plugin.internal.exceptions

internal class CannotParseLineException(line: String): Exception(
    "Could not parse line \"$line\""
)
