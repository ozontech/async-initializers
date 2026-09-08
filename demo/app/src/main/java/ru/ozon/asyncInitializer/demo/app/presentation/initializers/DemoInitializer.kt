package ru.ozon.asyncInitializer.demo.app.presentation.initializers

import ru.ozon.asyncInitializer.demo.app.domain.models.Initializer
import ru.ozon.asyncInitializer.library.ComponentInitializer
import ru.ozon.asyncInitializer.library.factory.AppComponentInitializerFactory
import ru.ozon.asyncInitializer.library.getComponentInitializer
import ru.ozon.asyncInitializer.library.store.ComponentInitializerStore

private val initializerCreateIdLocal = ThreadLocal<String>()

/**
 * Особенность апи создание [ComponentInitializer] осуществляется по классу который реализует данный класс
 * Для Demo приложения был развернут костыль с инициализацией по Id
 */
private fun runUseThreadLocal(id: String, action: () -> DemoInitializer): DemoInitializer {
    val previous = initializerCreateIdLocal.get()
    initializerCreateIdLocal.set(id)
    try {
        return action()
    } finally {
        when (previous) {
            null -> initializerCreateIdLocal.remove()
            else -> initializerCreateIdLocal.set(previous)
        }
    }
}

internal fun getDemoComponentInitializer(initializer: Initializer): DemoInitializer {
   return getDemoComponentInitializer(initializer.id)
}

internal fun getDemoComponentInitializer(dependencyId: String): DemoInitializer {
    return runUseThreadLocal(dependencyId) {
        getComponentInitializer<DemoInitializer>()
    }
}

internal class DemoFactoryAppComponentInitializerFactory(
    private val initializers: List<Initializer>
): AppComponentInitializerFactory {


    override fun <T : ComponentInitializer> create(initializer: Class<T>): T {
        val id = checkNotNull(initializerCreateIdLocal.get())
        @Suppress("UNCHECKED_CAST")
        return DemoInitializer(initializers.first { it.id == id }) as T
    }

}

internal class DemoComponentInitializerStore: ComponentInitializerStore {
    private val cache = mutableMapOf<String, ComponentInitializer>()

    override fun <T : ComponentInitializer> get(
        initializerKey: Class<T>,
    ): T? {
        val id = checkNotNull(initializerCreateIdLocal.get())
        val value = cache[id] ?: return null
        @Suppress("UNCHECKED_CAST")
        return value as T
    }

    override fun <T : ComponentInitializer> put(
        initializerKey: Class<T>,
        value: T,
    ) {
        val id = checkNotNull(initializerCreateIdLocal.get())
        cache[id] = value
    }

    fun clear() {
        cache.clear()
    }
}
internal class DemoInitializer(
    private val initializer: Initializer,
) : ComponentInitializer(runOnlyOnMainThread = initializer.runOnMainThread) {

    override fun runInitialize() {
        initializer.dependenciesIds.forEach { dependencyId ->
            getDemoComponentInitializer(dependencyId).initialize()
        }

        Thread.sleep(initializer.blockingTimeMs)
    }
}
