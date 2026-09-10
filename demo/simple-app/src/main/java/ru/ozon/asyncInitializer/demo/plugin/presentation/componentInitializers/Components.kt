package ru.ozon.asyncInitializer.demo.plugin.presentation.componentInitializers

/**
 * Components that require patching by the async-initializer-injector plugin
 *
 * The plugin injects `getComponentInitializer(T::class.java).initialize()` into every
 * public method based on ./demoComponentInitializerConfig.config. The commented lines
 * below are illustrative - they show what ends up in the final bytecode.
 */
object ComponentNetwork : BaseComponent {
    @Volatile
    private var initialized: Boolean = false

    fun initialize() {
//        getComponentInitializer(InitializerNetwork::class.java).initialize()
        initialized = true
    }

    override fun checkOnInitialize() {
//        getComponentInitializer(InitializerNetwork::class.java).initialize()
        check(initialized) { "Not initialize $this" }
    }
}

object ComponentStorage : BaseComponent {
    @Volatile
    private var initialized: Boolean = false

    fun initialize() {
//        getComponentInitializer(InitializerStorage::class.java).initialize()
        initialized = true
    }

    override fun checkOnInitialize() {
//        getComponentInitializer(InitializerStorage::class.java).initialize()
        check(initialized) { "Not initialize $this" }
    }
}

object ComponentCrypto : BaseComponent {
    @Volatile
    private var initialized: Boolean = false

    fun initialize() {
//        getComponentInitializer(InitializerCrypto::class.java).initialize()
        initialized = true
    }

    override fun checkOnInitialize() {
//        getComponentInitializer(InitializerCrypto::class.java).initialize()
        check(initialized) { "Not initialize $this" }
    }
}

object ComponentApiClient : BaseComponent {
    @Volatile
    private var initialized: Boolean = false

    fun initialize() {
//        getComponentInitializer(InitializerApiClient::class.java).initialize()
        initialized = true
    }

    override fun checkOnInitialize() {
//        getComponentInitializer(InitializerApiClient::class.java).initialize()
        check(initialized) { "Not initialize $this" }
    }
}

object ComponentDbManager : BaseComponent {
    @Volatile
    private var initialized: Boolean = false

    fun initialize() {
//        getComponentInitializer(InitializerDbManager::class.java).initialize()
        initialized = true
    }

    override fun checkOnInitialize() {
//        getComponentInitializer(InitializerDbManager::class.java).initialize()
        check(initialized) { "Not initialize $this" }
    }
}

object ComponentSession : BaseComponent {
    @Volatile
    private var initialized: Boolean = false

    fun initialize() {
//        getComponentInitializer(InitializerSession::class.java).initialize()
        initialized = true
    }

    override fun checkOnInitialize() {
//        getComponentInitializer(InitializerSession::class.java).initialize()
        check(initialized) { "Not initialize $this" }
    }
}

object ComponentFeatureFlags : BaseComponent {
    @Volatile
    private var initialized: Boolean = false

    fun initialize() {
//        getComponentInitializer(InitializerFeatureFlags::class.java).initialize()
        initialized = true
    }

    override fun checkOnInitialize() {
//        getComponentInitializer(InitializerFeatureFlags::class.java).initialize()
        check(initialized) { "Not initialize $this" }
    }
}

object ComponentSearchIndex : BaseComponent {
    @Volatile
    private var initialized: Boolean = false

    fun initialize() {
//        getComponentInitializer(InitializerSearchIndex::class.java).initialize()
        initialized = true
    }

    override fun checkOnInitialize() {
//        getComponentInitializer(InitializerSearchIndex::class.java).initialize()
        check(initialized) { "Not initialize $this" }
    }
}

object ComponentHomeScreen : BaseComponent {
    @Volatile
    private var initialized: Boolean = false

    fun initialize() {
//        getComponentInitializer(InitializerHomeScreen::class.java).initialize()
        initialized = true
    }

    override fun checkOnInitialize() {
//        getComponentInitializer(InitializerHomeScreen::class.java).initialize()
        check(initialized) { "Not initialize $this" }
    }
}

object ComponentProfileScreen : BaseComponent {
    @Volatile
    private var initialized: Boolean = false

    fun initialize() {
//        getComponentInitializer(InitializerProfileScreen::class.java).initialize()
        initialized = true
    }

    override fun checkOnInitialize() {
//        getComponentInitializer(InitializerProfileScreen::class.java).initialize()
        check(initialized) { "Not initialize $this" }
    }
}