package org.sandboxpowered.fabric.util

import com.google.gson.*
import org.graalvm.polyglot.Value

fun Value.getMemberValue(member: String): Value? = if (hasMember(member)) getMember(member) else null

fun Value.getMemberValueStr(member: String): String? = getMemberValue(member)?.asString()
fun Value.getMemberValueInt(member: String): Int? = getMemberValue(member)?.asInt()

fun Value.getMemberValue(member: String, default: String): String = getMemberValueStr(member) ?: default
fun Value.getMemberValue(member: String, default: Int): Int = getMemberValueInt(member) ?: default

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
    isNumber -> when {
        fitsInInt() -> JsonPrimitive(asInt())
        fitsInLong() -> JsonPrimitive(asLong())
        fitsInFloat() -> JsonPrimitive(asFloat())
        else -> JsonNull.INSTANCE
    }
    else -> JsonNull.INSTANCE
}