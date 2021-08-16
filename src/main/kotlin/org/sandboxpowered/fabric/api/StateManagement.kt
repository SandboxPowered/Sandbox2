package org.sandboxpowered.fabric.api

import com.google.common.hash.Hashing
import net.minecraft.state.property.*
import org.graalvm.polyglot.Value
import java.nio.charset.StandardCharsets

object StateManagement {
    val stateMap: MutableMap<String, PolyglotStateProperty> = hashMapOf()

    fun getStateProperty(name: String, type: String, vararg extra: Value): PolyglotStateProperty? {
        if (type == "boolean") {
            if (extra.isNotEmpty())
                throw IllegalArgumentException("Too many arguments specified, expected 0 got ${extra.size}")
            val hash = hash(name, type)
            return stateMap.computeIfAbsent(hash) { PolyglotStateProperty(null, name, type) }
        }
        return null
    }

    fun <T : Comparable<T>> putInternalStateProperty(property: Property<T>) {
        val name = property.name
        val type = when (property) {
            is BooleanProperty -> "boolean"
            is IntProperty -> "int"
            is DirectionProperty -> "direction"
            is EnumProperty -> "enum"
            else -> throw UnsupportedOperationException("Unknown property type ${property.javaClass}")
        }
        if (type == "int") {
            val properties = property.values.map { it as Int }.toTypedArray()

            val hash = hash(name, type, *properties.map { it.toString() }.toTypedArray())

            stateMap[hash] = PolyglotStateProperty(property, name, type, properties)
        } else if (type == "boolean") {
            val hash = hash(name, type)
            stateMap[hash] = PolyglotStateProperty(property, name, type)
        }
    }

    private fun hash(name: String, type: String, vararg extra: String): String {
        val compiled = buildString {
            append(name)
            append(type)
            extra.forEach { append(it) }
        }
        return Hashing.sha256()?.hashString(compiled, StandardCharsets.UTF_8)?.toString() ?: compiled
    }
}