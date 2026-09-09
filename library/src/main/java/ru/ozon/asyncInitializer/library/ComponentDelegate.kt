package ru.ozon.asyncInitializer.library

import ru.ozon.asyncInitializer.library.exceptions.BadInitializeComponentException
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

internal fun <T : Any> ComponentInitializer.lateSingleInitialize(): LateSingleInitialize<T> {
    return LateSingleInitialize()
}

/**
 * Delegate implementation guaranteeing deferred single initialization
 */
internal class LateSingleInitialize<T : Any> : ReadWriteProperty<ComponentInitializer, T> {
    private var value: T? = null

    override fun getValue(thisRef: ComponentInitializer, property: KProperty<*>): T {
        return value ?: throw BadInitializeComponentException()
    }

    override fun setValue(thisRef: ComponentInitializer, property: KProperty<*>, value: T) {
        if (this.value != null) throw BadInitializeComponentException()
        this.value = value
    }
}
