package ru.ozon.asyncInitializer.demo.plugin.presentation

import android.app.Application
import ru.ozon.asyncInitializer.demo.plugin.presentation.componentInitializers.InitializerA
import ru.ozon.asyncInitializer.demo.plugin.presentation.componentInitializers.InitializerB
import ru.ozon.asyncInitializer.demo.plugin.presentation.componentInitializers.InitializerC
import ru.ozon.asyncInitializer.library.ComponentInitializer
import ru.ozon.asyncInitializer.library.factory.AppComponentInitializerFactory
import ru.ozon.asyncInitializer.library.setupAppComponentInitializer

class App: Application() {

    override fun onCreate() {
        super.onCreate()

        setupAppComponentInitializer(
            factory = componentInitializerFactory()
        )
    }

    private fun componentInitializerFactory(): AppComponentInitializerFactory {
        return object : AppComponentInitializerFactory {
            override fun <T : ComponentInitializer> create(initializer: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return when (initializer) {
                    InitializerA::class.java -> InitializerA()
                    InitializerB::class.java -> InitializerB()
                    InitializerC::class.java -> InitializerC()
                    else -> error("Unsupported initializer")
                } as T
            }
        }
    }
}