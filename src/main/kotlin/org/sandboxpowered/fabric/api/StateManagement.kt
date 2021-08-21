package org.sandboxpowered.fabric.api

import com.google.common.hash.Hashing
import net.minecraft.state.property.Property
import org.graalvm.polyglot.Value
import java.nio.charset.StandardCharsets

object StateManagement {
    private val stateMap: MutableMap<String, PolyglotStateProperty> = hashMapOf()

    fun getStateProperty(name: String, type: String, vararg extra: Value): PolyglotStateProperty = when (type) {
        PolyglotStateProperty.BOOLEAN -> {
            require(extra.isEmpty()) { "Too many arguments specified, expected 0 got ${extra.size}" }
            stateMap.computeIfAbsent(hash(name, type)) { PolyglotStateProperty.from(name, type) }
        }
        PolyglotStateProperty.INT -> {
            val values = extra.map(Value::asInt).toTypedArray()
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
        hash(name, type, extra)

    private fun hash(name: String, type: String, extra: Array<out Any> = emptyArray()): String {
        val compiled = buildString {
            append(name)
            append(type)
            extra.forEach(this::append)
        }
        @Suppress("UnstableApiUsage") // apparently we ain't got anything better
        return Hashing.sha256()?.hashString(compiled, StandardCharsets.UTF_8)?.toString() ?: compiled
    }
}