package org.sandboxpowered.fabric.util

import com.google.gson.*
import org.graalvm.polyglot.Value


fun Value.getMemberValue(identifier: String): String? =
    if (hasMember(identifier)) getMember(identifier).asString() else null

fun Value.toJSON(): JsonElement = when {
    hasArrayElements() -> {
        val array = JsonArray()
        for (idx in 0 until arraySize) {
            val element = getArrayElement(idx)
            array.add(element.toJSON())
        }
        array
    }
    hasMembers() -> {
        val obj = JsonObject()
        for (memberKey in memberKeys) {
            obj.add(memberKey, getMember(memberKey).toJSON())
        }
        obj
    }
    isString -> JsonPrimitive(asString())
    isBoolean -> JsonPrimitive(asBoolean())
    isNumber -> JsonPrimitive(asInt())
    else -> JsonNull.INSTANCE
}