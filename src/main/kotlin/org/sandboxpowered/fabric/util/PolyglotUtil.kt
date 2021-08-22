package org.sandboxpowered.fabric.util

import com.google.gson.*
import org.graalvm.polyglot.Value


fun Value.getMemberValueStr(identifier: String): String? =
    if (hasMember(identifier)) getMember(identifier).asString() else null

fun Value.getMemberValue(identifier: String, default: String): String =
    if (hasMember(identifier)) getMember(identifier).asString() else default

fun Value.getMemberValueInt(identifier: String): Int? =
    if (hasMember(identifier)) getMember(identifier).asInt() else null

fun Value.getMemberValue(identifier: String, default: Int): Int =
    if (hasMember(identifier)) getMember(identifier).asInt() else default

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