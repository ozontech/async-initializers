package ru.ozon.asyncInitializer.app.data.model

import kotlinx.serialization.Serializable

@Serializable
internal data class InitializerDto(
    val name: String,
    val blockingTimeMs: Long = 1000L,
    val runOnMainThread: Boolean = false,
    val dependencies: List<String> = emptyList(),
)

@Serializable
internal data class InitializerConfigDto(
    val name: String,
    val initializers: List<InitializerDto>,
)
