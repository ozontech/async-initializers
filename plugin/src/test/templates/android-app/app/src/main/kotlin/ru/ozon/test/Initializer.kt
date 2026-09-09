package ru.ozon.test

import ru.ozon.android.componentInitiazlizer.ComponentInitializer

/**
 * Initializer referenced by inject.config.
 * Validation looks up the class by its name from the config.
 */
class InitializerA : ComponentInitializer() {

    override fun runInitialize() {
        // body does not matter for patching checks
    }
}