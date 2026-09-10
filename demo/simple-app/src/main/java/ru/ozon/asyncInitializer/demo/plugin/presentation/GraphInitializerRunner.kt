package ru.ozon.asyncInitializer.demo.plugin.presentation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import ru.ozon.asyncInitializer.demo.plugin.presentation.componentInitializers.ComponentApiClient
import ru.ozon.asyncInitializer.demo.plugin.presentation.componentInitializers.ComponentCrypto
import ru.ozon.asyncInitializer.demo.plugin.presentation.componentInitializers.ComponentDbManager
import ru.ozon.asyncInitializer.demo.plugin.presentation.componentInitializers.ComponentFeatureFlags
import ru.ozon.asyncInitializer.demo.plugin.presentation.componentInitializers.ComponentHomeScreen
import ru.ozon.asyncInitializer.demo.plugin.presentation.componentInitializers.ComponentNetwork
import ru.ozon.asyncInitializer.demo.plugin.presentation.componentInitializers.ComponentProfileScreen
import ru.ozon.asyncInitializer.demo.plugin.presentation.componentInitializers.ComponentSearchIndex
import ru.ozon.asyncInitializer.demo.plugin.presentation.componentInitializers.ComponentSession
import ru.ozon.asyncInitializer.demo.plugin.presentation.componentInitializers.ComponentStorage

private val components = listOf(
    ComponentNetwork,
    ComponentStorage,
    ComponentCrypto,
    ComponentApiClient,
    ComponentDbManager,
    ComponentSession,
    ComponentFeatureFlags,
    ComponentSearchIndex,
    ComponentHomeScreen,
    ComponentProfileScreen,
)

object GraphInitializerRunner {

    private val scope = CoroutineScope(Dispatchers.IO)

    fun runAsync()  {
        if (InitializerStatus.wasStarted) return
        InitializerStatus.run()

        scope.launch {
            components
                .map { component -> async { component.checkOnInitialize() } }
                .awaitAll()
        }
    }

    fun runSync()  {
        if (InitializerStatus.wasStarted) return
        InitializerStatus.run()

        scope.launch {
            components
                .forEach { component -> component.checkOnInitialize() }

        }
    }

}