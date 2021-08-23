package org.sandboxpowered.fabric.util

import com.google.gson.*
import org.graalvm.polyglot.Value


fun Value.getMemberValue(identifier: String): String? =
    if (hasMember(identifier)) getMember(identifier).asString() else null

fun Value.toJSON(): JsonElement = when {
    hasArrayElements() -> JsonArray().apply {
        (0 until arraySize)
            .map { getArrayElement(it).toJSON() }
            .forEach(::add)
    }
    hasMembers() -> JsonObject().apply {
        memberKeys.forEach { add(it, getMember(it).toJSON()) }
    }
    isString -> JsonPrimitive(asString())
    isBoolean -> JsonPrimitive(asBoolean())
    isNumber -> JsonPrimitive(asInt())
    else -> JsonNull.INSTANCE
}