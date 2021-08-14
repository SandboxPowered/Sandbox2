package org.sandboxpowered.fabric.scripting.polyglot

import com.google.gson.JsonElement
import net.minecraft.util.Identifier
import org.graalvm.polyglot.HostAccess.Export
import org.graalvm.polyglot.Value
import java.util.function.BiPredicate

class PolyglotRecipeManager(private val map: MutableMap<Identifier, JsonElement>) {

    private val removalPredicates: ArrayList<BiPredicate<Identifier, JsonElement>> = arrayListOf()
    private var removeAll: Boolean = false

    fun run() {
        if (removeAll) map.clear() else {
            val toRemove = arrayListOf<Identifier>()
            map.forEach { (t, u) ->
                removalPredicates.forEach {
                    if (it.test(t, u))
                        toRemove.add(t)
                }
            }
            toRemove.forEach(map::remove)
        }
    }

    @Export
    fun remove(value: Value) {
        if (!value.hasMembers()) throw UnsupportedOperationException("Unsupported value in recipe removal")

        var predicate: BiPredicate<Identifier, JsonElement>? = null

        val id = value.getMemberValue("id")
        val output = value.getMemberValue("output")
        val domain = value.getMemberValue("domain")
        val type = value.getMemberValue("type")

        if (id != null) {
            val idPredicate = BiPredicate<Identifier, JsonElement> { identifier, _ -> identifier.toString() == id }
            predicate = mergePredicates(predicate, idPredicate)
        }
        if (domain != null) {
            val domainPredicate =
                BiPredicate<Identifier, JsonElement> { identifier, _ -> identifier.namespace == domain }
            predicate = mergePredicates(predicate, domainPredicate)
        }
        if (type != null) {
            val typePredicate =
                BiPredicate<Identifier, JsonElement> { _, json -> json.asJsonObject.get("type").asString == type }
            predicate = mergePredicates(predicate, typePredicate)
        }
        if (output != null) {
            val outputPredicate = BiPredicate<Identifier, JsonElement> { _, _ -> false }
            predicate = mergePredicates(predicate, outputPredicate)
        }

        if (predicate != null)
            removalPredicates.add(predicate)
        else
            removeAll = true
    }

    private fun mergePredicates(
        predicate: BiPredicate<Identifier, JsonElement>?,
        newPredicate: BiPredicate<Identifier, JsonElement>
    ): BiPredicate<Identifier, JsonElement>? {
        if (predicate != null)
            return predicate.and(newPredicate)
        return newPredicate
    }
}

private fun Value.getMemberValue(identifier: String): String? =
    if (hasMember(identifier)) getMember(identifier).asString() else null
