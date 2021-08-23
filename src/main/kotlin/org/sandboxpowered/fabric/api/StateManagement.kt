package org.sandboxpowered.fabric.api

import net.minecraft.state.property.Property
import org.graalvm.polyglot.Value
import org.sandboxpowered.fabric.util.Hash
import org.sandboxpowered.fabric.util.hash

object StateManagement {
    private val stateMap: MutableMap<String, PolyglotStateProperty> = hashMapOf()

    fun getStateProperty(name: String, type: String, vararg extra: Value): PolyglotStateProperty = when (type) {
        PolyglotStateProperty.BOOLEAN -> {
            require(extra.isEmpty()) { "Too many arguments specified, expected 0 got ${extra.size}" }
            stateMap.computeIfAbsent(hash(name, type)) { PolyglotStateProperty.from(name, type) }
        }
        PolyglotStateProperty.INT -> {
            require(extra.size == 2) { "Invalid number of arguments specified, expected 2 got ${extra.size}" }
            var values = extra.map(Value::asInt).toTypedArray()

            values = (values[0]..values[1]).toList().toTypedArray()
            stateMap.computeIfAbsent(hash(name, type, values)) {
                PolyglotStateProperty.from(name, type, values)
            }
        }
        PolyglotStateProperty.DIRECTION, PolyglotStateProperty.ENUM -> TODO("Not yet implemented")
        else -> throw IllegalArgumentException("Unknown type `$type`")
    }

    fun <T : Comparable<T>> putInternalStateProperty(property: Property<T>) {
        val polyProp = PolyglotStateProperty from property
        stateMap[polyProp.hash()] = polyProp
    }

    private fun PolyglotStateProperty.hash(): String =
        hash(name, type, values)

    private fun hash(name: String, type: String, extra: Array<*> = emptyArray<Any>()): String {
        val compiled = buildString {
            append(name)
            append(type)
            extra.forEach(this::append)
        }
        return compiled.hash(Hash.SHA256)
    }
}