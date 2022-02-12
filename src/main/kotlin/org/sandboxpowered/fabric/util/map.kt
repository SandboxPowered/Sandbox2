package org.sandboxpowered.fabric.util

import com.google.common.collect.ImmutableMap

/**
 * Removes elements from this map if the predicate returns true
 */
inline fun <K, V> MutableMap<K, V>.removeIf(predicate: (K, V) -> Boolean) {
    val iter = iterator()
    while (iter.hasNext()) {
        val (k, v) = iter.next()
        if (predicate(k, v)) iter.remove()
    }
}

operator fun <K, V> ImmutableMap.Builder<K, V>.set(key: K, value: V): ImmutableMap.Builder<K, V> {
    return put(key, value)
}