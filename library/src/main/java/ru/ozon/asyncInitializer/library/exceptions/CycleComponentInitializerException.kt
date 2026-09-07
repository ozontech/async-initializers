package ru.ozon.asyncInitializer.library.exceptions

public class CycleComponentInitializerException internal constructor(
    order: Iterable<String>,
    repeatedClass: String,
) : Exception(
    "Циклическая зависимость при создании: ${order.joinToString(""){ "$it->" }}$repeatedClass",
)
