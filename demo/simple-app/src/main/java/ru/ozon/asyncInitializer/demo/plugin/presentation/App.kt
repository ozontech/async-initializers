package ru.ozon.asyncInitializer.demo.plugin.presentation

import android.app.Application
import ru.ozon.asyncInitializer.demo.plugin.presentation.componentInitializers.InitializerApiClient
import ru.ozon.asyncInitializer.demo.plugin.presentation.componentInitializers.InitializerCrypto
import ru.ozon.asyncInitializer.demo.plugin.presentation.componentInitializers.InitializerDbManager
import ru.ozon.asyncInitializer.demo.plugin.presentation.componentInitializers.InitializerFeatureFlags
import ru.ozon.asyncInitializer.demo.plugin.presentation.componentInitializers.InitializerHomeScreen
import ru.ozon.asyncInitializer.demo.plugin.presentation.componentInitializers.InitializerNetwork
import ru.ozon.asyncInitializer.demo.plugin.presentation.componentInitializers.InitializerProfileScreen
import ru.ozon.asyncInitializer.demo.plugin.presentation.componentInitializers.InitializerSearchIndex
import ru.ozon.asyncInitializer.demo.plugin.presentation.componentInitializers.InitializerSession
import ru.ozon.asyncInitializer.demo.plugin.presentation.componentInitializers.InitializerStorage
import ru.ozon.asyncInitializer.library.ComponentInitializer
import ru.ozon.asyncInitializer.library.factory.AppComponentInitializerFactory
import ru.ozon.asyncInitializer.library.setupAppComponentInitializer

class App: Application() {

    override fun onCreate() {
        super.onCreate()

        setupAppComponentInitializer(
            factory = asyncFactory(),
        )
    }

    private fun asyncFactory() = object : AppComponentInitializerFactory {

        override fun <T : ComponentInitializer> create(initializer: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return when (initializer) {
                InitializerNetwork::class.java -> InitializerNetwork()
                InitializerStorage::class.java -> InitializerStorage()
                InitializerCrypto::class.java -> InitializerCrypto()
                InitializerApiClient::class.java -> InitializerApiClient()
                InitializerDbManager::class.java -> InitializerDbManager()
                InitializerSession::class.java -> InitializerSession()
                InitializerFeatureFlags::class.java -> InitializerFeatureFlags()
                InitializerSearchIndex::class.java -> InitializerSearchIndex()
                InitializerHomeScreen::class.java -> InitializerHomeScreen()
                InitializerProfileScreen::class.java -> InitializerProfileScreen()
                else -> error("Unsupported initializer")
            } as T
        }
    }
}