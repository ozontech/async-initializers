package ru.ozon.asyncInitializers.plugin.internal.util

internal typealias MutableOrderedMap<K, V> = LinkedHashMap<K, V>

internal fun <K,V> MutableOrderedMap<K, V>.toImmutable(): ImmutableOrderedMap<K, V> = ImmutableOrderedMap(this)

@JvmInline
internal value class ImmutableOrderedMap<K, V>(
    private val orderedMap: MutableOrderedMap<K,V>
): Map<K,V> by orderedMap
