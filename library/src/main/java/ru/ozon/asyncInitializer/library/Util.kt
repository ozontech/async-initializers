package ru.ozon.asyncInitializer.library

internal fun <T> emptyIterator(): Iterator<T> {
    return object : Iterator<T> {
        override fun hasNext(): Boolean = false
        override fun next(): Nothing { throw NoSuchElementException() }
    }
}
