package ru.ozon.asyncInitializer.demo.app.data.mapper

import ru.ozon.asyncInitializer.demo.app.data.model.InitializerConfigDto
import ru.ozon.asyncInitializer.demo.app.data.model.InitializerDto
import ru.ozon.asyncInitializer.demo.app.domain.models.Initializer
import ru.ozon.asyncInitializer.demo.app.domain.models.InitializerConfig
import java.util.UUID

internal fun InitializerConfigDto.toEntity(): InitializerConfig {
    val idToNameMap: Map<String, String> = initializers.associateBy(
        keySelector = { it.name },
        valueTransform = { UUID.randomUUID().toString() }
    )

    return InitializerConfig(
        name = name,
        initializers = initializers.map{ initializer -> initializer.toEntity(idToNameMap) }
    )
}

internal fun InitializerDto.toEntity(idToNameMap: Map<String, String>): Initializer {
    return Initializer(
        id = checkNotNull(idToNameMap[name]),
        name = name,
        blockingTimeMs = blockingTimeMs,
        runOnMainThread = runOnMainThread,
        dependenciesIds = buildSet {
            dependencies.forEach { dependencyName ->
                val id = checkNotNull(checkNotNull(idToNameMap[dependencyName]))
                add(id)
            }
        }
    )
}
