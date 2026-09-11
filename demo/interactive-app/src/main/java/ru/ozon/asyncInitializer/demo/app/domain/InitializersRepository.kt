package ru.ozon.asyncInitializer.demo.app.domain

import ru.ozon.asyncInitializer.demo.app.domain.models.Initializer
import ru.ozon.asyncInitializer.demo.app.domain.models.InitializerConfig

internal interface InitializersRepository {

    suspend fun loadPreloadConfigs(): List<InitializerConfig>

    fun saveInitializers(initializers: List<Initializer>)

    fun getSavedInitializers(): List<Initializer>
}
