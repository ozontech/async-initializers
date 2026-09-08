package ru.ozon.asyncInitializers.plugin.internal.exceptions

internal class NonConsistentLineException: Exception(
  "Неправильное состояние LineReader: ожидалось, что currentLine не может быть равен null"
)
