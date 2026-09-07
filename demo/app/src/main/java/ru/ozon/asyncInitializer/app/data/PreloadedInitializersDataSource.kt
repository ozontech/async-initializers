package ru.ozon.asyncInitializer.app.data

import android.app.Application
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import ru.ozon.asyncInitializer.app.data.model.InitializerConfigDto

internal class PreloadedInitializersDataSource(
    private val context: Application
) {
    @Volatile
    private var cache: List<InitializerConfigDto>? = null

    private val mutex = Mutex()

    suspend fun loadConfigurations(): List<InitializerConfigDto> {
        var localCache = cache
        if (localCache != null) return localCache

        mutex.withLock {
            localCache = cache
            if (localCache != null) return localCache

            val namesFiles = context.assets.list("")
            localCache = withContext(Dispatchers.IO) {
                buildList {
                    namesFiles?.forEach { nameFile ->
                        if (nameFile.endsWith(".json")) {
                            try {
                                val jsonString = context.assets.open(nameFile)
                                    .bufferedReader()
                                    .use { it.readText() }
                                val config = Json.decodeFromString<InitializerConfigDto>(jsonString)
                                config.validate()
                                add(config)
                            } catch (_: Exception) {

                            }
                        }
                    }
                }
            }

            cache = localCache
            return localCache
        }

    }

    private fun InitializerConfigDto.validate() {
        val uniqueNames = buildSet { initializers.forEach { add(it.name) } }
        if (uniqueNames.size != initializers.size) error("The name must be unique")
    }
}
