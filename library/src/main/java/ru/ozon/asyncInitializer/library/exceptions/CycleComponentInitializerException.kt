package ru.ozon.asyncInitializer.library.exceptions

public class CycleComponentInitializerException internal constructor(
    order: Iterable<String>,
    repeatedClass: String,
) : Exception(
    "Cyclic dependency detected while creating: ${order.joinToString(""){ "$it->" }}$repeatedClass",
)
