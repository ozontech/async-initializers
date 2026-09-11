package ru.ozon.asyncInitializer.demo.plugin.presentation.componentInitializers

import ru.ozon.asyncInitializer.demo.plugin.presentation.InitializerStatus
import ru.ozon.asyncInitializer.library.ComponentInitializer

private fun running(componentKey: ComponentKeys, blockingTime: Long, initialize: () -> Unit) {
    InitializerStatus.running(componentKey)
    Thread.sleep(blockingTime)
    initialize()
    InitializerStatus.finished(componentKey)
}

class InitializerNetwork : ComponentInitializer() {

    override fun runInitialize() {
        running(ComponentKeys.NETWORK, 2000) { ComponentNetwork.initialize() }
    }
}

class InitializerStorage : ComponentInitializer() {

    override fun runInitialize() {
        running(ComponentKeys.STORAGE, 1200) { ComponentStorage.initialize() }
    }
}

class InitializerCrypto : ComponentInitializer() {

    override fun runInitialize() {
        running(ComponentKeys.CRYPTO, 1000) { ComponentCrypto.initialize() }
    }
}

class InitializerApiClient : ComponentInitializer() {

    override fun runInitialize() {
        ComponentNetwork.checkOnInitialize()
        ComponentCrypto.checkOnInitialize()

        running(ComponentKeys.API_CLIENT, 850) { ComponentApiClient.initialize() }
    }
}

class InitializerDbManager : ComponentInitializer() {

    override fun runInitialize() {
        ComponentStorage.checkOnInitialize()
        ComponentCrypto.checkOnInitialize()

        running(ComponentKeys.DB_MANAGER, 700) { ComponentDbManager.initialize() }
    }
}

class InitializerSession : ComponentInitializer() {

    override fun runInitialize() {
        ComponentApiClient.checkOnInitialize()

        running(ComponentKeys.SESSION, 250) { ComponentSession.initialize() }
    }
}

class InitializerFeatureFlags : ComponentInitializer() {

    override fun runInitialize() {
        ComponentApiClient.checkOnInitialize()

        running(ComponentKeys.FEATURE_FLAGS, 500) { ComponentFeatureFlags.initialize() }
    }
}

class InitializerSearchIndex : ComponentInitializer() {

    override fun runInitialize() {
        ComponentDbManager.checkOnInitialize()

        running(ComponentKeys.SEARCH_INDEX, 2000) { ComponentSearchIndex.initialize() }
    }
}

class InitializerHomeScreen : ComponentInitializer() {

    override fun runInitialize() {
        ComponentSession.checkOnInitialize()
        ComponentFeatureFlags.checkOnInitialize()
        ComponentSearchIndex.checkOnInitialize()

        running(ComponentKeys.HOME_SCREEN, 1000) { ComponentHomeScreen.initialize() }
    }
}

class InitializerProfileScreen : ComponentInitializer() {

    override fun runInitialize() {
        ComponentSession.checkOnInitialize()
        ComponentDbManager.checkOnInitialize()

        running(ComponentKeys.PROFILE_SCREEN, 800) { ComponentProfileScreen.initialize() }
    }
}