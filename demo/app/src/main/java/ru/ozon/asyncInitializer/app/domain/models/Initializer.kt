package ru.ozon.asyncInitializer.app.domain.models

import java.util.UUID

internal data class Initializer(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val blockingTimeMs: Long = 1000L,
    val runOnMainThread: Boolean = false,
    val dependenciesIds: Set<String> = emptySet(),
)

internal data class InitializerConfig(
    val name: String,
    val initializers: List<Initializer>
)
