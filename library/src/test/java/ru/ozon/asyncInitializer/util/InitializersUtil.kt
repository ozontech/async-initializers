package ru.ozon.asyncInitializer.util

import ru.ozon.asyncInitializer.library.ComponentInitializer
import ru.ozon.asyncInitializer.library.factory.AppComponentInitializerFactory
import kotlin.reflect.KClass

class DynamicLazyInitializeComponentInitializerFactory(
    initializers: Map<KClass<out ComponentInitializer>, () -> ComponentInitializer> = emptyMap(),
) : AppComponentInitializerFactory {

    private val initializers = initializers.toMutableMap()

    constructor(vararg initializers: Pair<KClass<out ComponentInitializer>, () -> ComponentInitializer>) : this(initializers.toMap())

    fun modify(initializers: List<Pair<KClass<out ComponentInitializer>, () -> ComponentInitializer>>) {
        this.initializers.putAll(initializers)
    }

    override fun <T : ComponentInitializer> create(
        initializerKey: Class<T>,
    ): T {
        val foundInitializer = initializers[initializerKey.kotlin]
            ?: error("Initializer not found")

        @Suppress("UNCHECKED_CAST")
        return foundInitializer() as T
    }
}
class LazyInitializeComponentInitializerFactory(
    private val initializers: Map<KClass<out ComponentInitializer>, () -> ComponentInitializer>,
) : AppComponentInitializerFactory {
    constructor(vararg initializers: Pair<KClass<out ComponentInitializer>, () -> ComponentInitializer>) : this(initializers.toMap())

    override fun <T : ComponentInitializer> create(
        initializerKey: Class<T>,
    ): T {
        val foundInitializer = initializers[initializerKey.kotlin]
            ?: error("Initializer not found")

        @Suppress("UNCHECKED_CAST")
        return foundInitializer() as T
    }
}

class ListInitializeComponentInitializerFactory(
    private val initializers: List<ComponentInitializer>,
) : AppComponentInitializerFactory {
    constructor(vararg initializers: ComponentInitializer) : this(initializers.toList())

    override fun <T : ComponentInitializer> create(
        initializerKey: Class<T>,
    ): T {
        val foundInitializer = initializers.find { initializer -> initializer::class.java == initializerKey }
            ?: error("Initializer not found")

        @Suppress("UNCHECKED_CAST")
        return foundInitializer as T
    }
}

class SingleInitializeComponentInitializerFactory(
    private val componentInitializer: ComponentInitializer,
) : AppComponentInitializerFactory {
    override fun <T : ComponentInitializer> create(
        initializer: Class<T>,
    ): T {
        @Suppress("UNCHECKED_CAST")
        return componentInitializer as T
    }
}

object EmptyAppComponentInitializerFactory : AppComponentInitializerFactory {
    override fun <T : ComponentInitializer> create(
        initializer: Class<T>,
    ): T {
        error("Not supported")
    }
}

fun FuncComponentInitializer(runOnlyOnMainThread: Boolean = false, action: () -> Unit): ComponentInitializer {
    return object : ComponentInitializer(runOnlyOnMainThread) {
        override fun runInitialize() { action() }
    }
}
