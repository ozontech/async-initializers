package ru.ozon.asyncInitializers.plugin.internal.exceptions

internal class NonConsistentLineException: Exception(
  "Invalid LineReader state: expected currentLine not to be null"
)
