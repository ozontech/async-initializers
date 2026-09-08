package ru.ozon.test

/**
 * Класс-жертва. Плагин встраивает в публичные методы
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