package ru.ozon.asyncInitializer.demo.app.data

import ru.ozon.asyncInitializer.demo.app.data.mapper.toEntity
import ru.ozon.asyncInitializer.demo.app.data.model.InitializerConfigDto
import ru.ozon.asyncInitializer.demo.app.domain.InitializersRepository
import ru.ozon.asyncInitializer.demo.app.domain.models.Initializer
import ru.ozon.asyncInitializer.demo.app.domain.models.InitializerConfig

internal class InitializersRepositoryImpl(
    private val preloadDataSource: PreloadedInitializersDataSource
): InitializersRepository {


    override suspend fun loadPreloadConfigs(): List<InitializerConfig> = preloadDataSource
        .loadConfigurations()
        .map(InitializerConfigDto::toEntity)

    override fun saveInitializers(initializers: List<Initializer>) {
        SavedInitializersStore.initializers = initializers
    }

    override fun getSavedInitializers(): List<Initializer> = SavedInitializersStore.initializers
}

private object SavedInitializersStore {

    @Volatile
    var initializers: List<Initializer> = emptyList()

}
