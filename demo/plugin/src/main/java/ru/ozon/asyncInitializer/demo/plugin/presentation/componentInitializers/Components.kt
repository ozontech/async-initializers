package ru.ozon.asyncInitializer.demo.plugin.presentation.componentInitializers

/**
 * Examples of components that require patching
 *
 * After applying the plugin with the component-initializer-injector config ./demoComponentInitializerConfig.config
 * A sample is included where lines showing the final file are added as comments
 */
object ComponentA {
    @Volatile
    private var message: String? = null

    fun initialize(message: String) {
//        getComponentInitializer(InitializerA::class.java).initialize()
        this.message = message
    }

    fun get(): String {
//        getComponentInitializer(InitializerA::class.java).initialize()
        return message ?: error("Not initialize $this")
    }
}
object ComponentB {
    @Volatile
    private var message: String? = null

    fun initialize(message: String) {
//        getComponentInitializer(InitializerB::class.java).initialize()
        this.message = message
    }

    fun get(): String {
//        getComponentInitializer(InitializerB::class.java).initialize()
        return message ?: error("Not initialize $this")
    }
}
object ComponentC {
    @Volatile
    private var message: String? = null

    fun initialize(message: String) {
//        getComponentInitializer(InitializerC::class.java).initialize()
        this.message = message
    }

    fun get(): String {
//        getComponentInitializer(InitializerC::class.java).initialize()
        return message ?: error("Not initialize $this")
    }
}
