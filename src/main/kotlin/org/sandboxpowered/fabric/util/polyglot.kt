package org.sandboxpowered.fabric.util

import com.google.gson.*
import org.graalvm.polyglot.Value


fun Value.getMemberValue(member: String): Value? = if (hasMember(member)) getMember(member) else null

fun Value.getMemberValueStr(member: String): String? = getMemberValue(member)?.asString()
fun Value.getMemberValueInt(member: String): Int? = getMemberValue(member)?.asInt()
fun Value.getMemberValueFloat(member: String): Float? = getMemberValue(member)?.asFloat()

fun Value.getMemberValue(member: String, default: String): String = getMemberValueStr(member) ?: default
fun Value.getMemberValue(member: String, default: Int): Int = getMemberValueInt(member) ?: default
fun Value.getMemberValue(member: String, default: Float): Float = getMemberValueFloat(member) ?: default

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

inline fun <reified T> Value.convert(): T {
    return `as`(T::class.java)
}

fun Value.asArray(): Array<Value> {
    if (hasArrayElements()) {
        return (0 until this.arraySize).map { getArrayElement(it) }.toTypedArray()
    }
    return emptyArray()
}
