package ru.ozon.asyncInitializer.library.cycleCallDetector

import ru.ozon.asyncInitializer.library.ComponentInitializer

internal data class InitializerCallStackFrame(
    val uniqueId: String,
    val initializerClass: Class<out ComponentInitializer>,
) {
    constructor(initializer: ComponentInitializer) : this(initializer.uniqueId, initializer.javaClass)
}
