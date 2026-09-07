package ru.ozon.asyncInitializer.app.domain

import ru.ozon.asyncInitializer.app.domain.models.Initializer
import ru.ozon.asyncInitializer.app.domain.models.InitializerConfig

internal interface InitializersRepository {

    suspend fun loadPreloadConfigs(): List<InitializerConfig>

    fun saveInitializers(initializers: List<Initializer>)

    fun getSavedInitializers(): List<Initializer>
}
