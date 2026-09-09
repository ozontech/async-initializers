package ru.ozon.test

/**
 * Victim class. The plugin embeds into public methods
 * "getComponentInitializer(InitializerA::class.java).initialize()"
 */
object ComponentA {

    @Volatile
    private var value: String? = null

    fun initialize(value: String) {
        this.value = value
    }

    fun get(): String {
        return value ?: "not initialized"
    }
}