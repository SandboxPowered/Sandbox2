package org.sandboxpowered.fabric.util

import com.google.gson.*
import org.graalvm.polyglot.Value


fun Value.getMemberValue(identifier: String): String? =
    if (hasMember(identifier)) getMember(identifier).asString() else null

fun Value.toJSON(): JsonElement {
    when {
        hasArrayElements() -> {
            val array = JsonArray()
            for (idx in 0 until arraySize) {
                val element = getArrayElement(idx)
                array.add(element.toJSON())
            }
            return array
        }
        hasMembers() -> {
            val obj = JsonObject()
            for (memberKey in memberKeys) {
                obj.add(memberKey, getMember(memberKey).toJSON())
            }
            return obj
        }
        isString -> {
            return JsonPrimitive(asString())
        }
        isBoolean -> {
            return JsonPrimitive(asBoolean())
        }
        isNumber -> {
            return JsonPrimitive(asInt())
        }
        else -> return JsonNull.INSTANCE
    }
}