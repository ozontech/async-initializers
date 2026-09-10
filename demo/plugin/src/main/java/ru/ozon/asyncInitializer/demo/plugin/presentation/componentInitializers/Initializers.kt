package ru.ozon.asyncInitializer.demo.plugin.presentation.componentInitializers

import ru.ozon.asyncInitializer.library.ComponentInitializer


class InitializerA: ComponentInitializer() {

    override fun runInitialize() {
        val message = buildString {
            appendLine(ComponentB.get())
            append("Initialize A")
        }

        ComponentA.initialize(message)
    }
}

class InitializerB: ComponentInitializer() {

    override fun runInitialize() {
        val message = buildString {
            appendLine(ComponentC.get())
            append("Initialize B")
        }

        ComponentB.initialize(message)
    }
}

class InitializerC: ComponentInitializer() {

    override fun runInitialize() {
        val message = buildString {
            append("Initialize C")
        }

        ComponentC.initialize(message)
    }
}
