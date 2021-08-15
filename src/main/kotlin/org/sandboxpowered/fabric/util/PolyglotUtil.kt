package org.sandboxpowered.fabric.util

import com.google.gson.*
import org.graalvm.polyglot.Value


fun Value.getMemberValue(identifier: String): String? =
    if (hasMember(identifier)) getMember(identifier).asString() else null

fun Value.toJSON(): JsonElement = when {
    hasArrayElements() -> {
        val array = JsonArray()
        (0 until arraySize)
            .map { getArrayElement(it).toJSON() }
            .forEach(array::add)
        array
    }
    hasMembers() -> {
        val obj = JsonObject()
        memberKeys.forEach { obj.add(it, getMember(it).toJSON()) }
        obj
    }
    isString -> JsonPrimitive(asString())
    isBoolean -> JsonPrimitive(asBoolean())
    isNumber -> JsonPrimitive(asInt())
    else -> JsonNull.INSTANCE
}