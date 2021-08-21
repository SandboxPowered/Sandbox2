package org.sandboxpowered.fabric.util

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