package ru.ozon.asyncInitializers.plugin.internal.exceptions

internal class CannotParseLineException(line: String): Exception(
    "Не получилось распарсить строку \"$line\""
)
