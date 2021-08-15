package org.sandboxpowered.fabric.scripting.polyglot

import com.google.gson.Gson
import com.google.gson.JsonElement
import net.minecraft.item.Item
import net.minecraft.tag.Tag
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import org.graalvm.polyglot.HostAccess.Export
import org.graalvm.polyglot.Value
import org.sandboxpowered.fabric.Main
import org.sandboxpowered.fabric.util.getMemberValue
import org.sandboxpowered.fabric.util.toJSON
import java.util.function.BiPredicate

class PolyglotRecipeManager(private val map: MutableMap<Identifier, JsonElement>) {

    private val removalPredicates: MutableList<BiPredicate<Identifier, JsonElement>> = arrayListOf()
    private val newRecipes: MutableList<JsonElement> = arrayListOf()
    private var removeAll: Boolean = false
    private val gson = Gson()

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
        var id = 0
        newRecipes.forEach {
            map[Identifier("sandbox", "recipe-${id++}")] = it
        }
    }

    @Export
    fun add(value: Value) {
        if (!value.hasMembers()) throw UnsupportedOperationException("Unsupported value in recipe creation")
        newRecipes.add(value.toJSON())
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
            val domainPredicate = BiPredicate<Identifier, JsonElement> { identifier, _ ->
                identifier.namespace == domain
            }
            predicate = mergePredicates(predicate, domainPredicate)
        }
        if (type != null) {
            val typePredicate = BiPredicate<Identifier, JsonElement> { _, json ->
                json.asJsonObject.get("type").asString == type
            }
            predicate = mergePredicates(predicate, typePredicate)
        }
        if (output != null) {
            var tag: Tag<Item>? = null
            val outputPredicate = BiPredicate<Identifier, JsonElement> { id, json ->
                val obj = json.asJsonObject
                if(!obj.has("result")) {
                    return@BiPredicate false
                }
                val resultElement = obj.get("result")
                val resultString = if(resultElement.isJsonObject) {
                    resultElement.asJsonObject.get("item").asString
                } else {
                    resultElement.asString
                }
                if(output.startsWith('#')) {
                    if(tag==null) {
                        tag = Main.resourceManager.registryTagManager.getTag(Registry.ITEM_KEY, Identifier(output.substring(1))) {
                            NullPointerException(it.toString())
                        }
                    }
                    return@BiPredicate tag!!.contains(Registry.ITEM.get(Identifier(resultString)))
                }
                resultString == output
            }
            predicate = mergePredicates(predicate, outputPredicate)
        }

        if (predicate != null) removalPredicates += predicate
        else removeAll = true
    }

    private fun mergePredicates(
        predicate: BiPredicate<Identifier, JsonElement>?,
        newPredicate: BiPredicate<Identifier, JsonElement>
    ): BiPredicate<Identifier, JsonElement> {
        return predicate?.and(newPredicate) ?: newPredicate
    }
}
