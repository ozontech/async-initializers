package ru.ozon.asyncInitializer.demo.plugin.presentation.componentInitializers

/**
 * Примеры возможных компонентов которым необходим патчинг
 *
 * После применения к ним плагина с конфигом component-initializer-injector с конфигурацией ./demoComponentInitializerConfig.config
 * Прилагается образец где в комментариями добавлены строки как будет выглядеть финальный файл
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
