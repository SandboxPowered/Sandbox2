package org.sandboxpowered.fabric.api

import com.google.gson.JsonElement
import net.minecraft.item.Item
import net.minecraft.tag.Tag
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import org.graalvm.polyglot.HostAccess.Export
import org.graalvm.polyglot.Value
import org.sandboxpowered.fabric.Main
import org.sandboxpowered.fabric.util.getMemberValue
import org.sandboxpowered.fabric.util.removeIf
import org.sandboxpowered.fabric.util.toJSON
import java.util.function.BiPredicate

class PolyglotRecipeManager(private val map: MutableMap<Identifier, JsonElement>) {

    private val removalPredicates: MutableList<BiPredicate<Identifier, JsonElement>> = arrayListOf()
    private val newRecipes: MutableList<JsonElement> = arrayListOf()
    private var removeAll: Boolean = false
    private val inputReplacement: MutableList<Triple<String, String, BiPredicate<Identifier, JsonElement>?>> =
        arrayListOf()

    fun run() {
        if (removeAll) map.clear()
        else map.removeIf { id, json -> removalPredicates.any { it.test(id, json) } }
        var id = 0
        newRecipes.forEach {
            map[Identifier("sandbox", "recipe-${id++}")] = it
        }
        inputReplacement.forEach { (input, output, filter) ->
            map.forEach { (id, json) ->
                if (filter == null || filter.test(id, json)) {
                    TODO("Find input values and replace")
                }
            }
        }
    }

    @Export
    fun add(value: Value) {
        require(value.hasMembers()) { "Unsupported value as custom recipe input" }
        newRecipes.add(value.toJSON())
    }

    @Export
    fun replaceInput(input: String, output: String, filterInput: Value?) {
        val filter = filterInput?.let(this::convertValueToRecipeFilter)

        inputReplacement.add(Triple(input, output, filter))
    }

    private fun convertValueToRecipeFilter(value: Value): BiPredicate<Identifier, JsonElement>? {
        require(value.hasMembers()) { "Unsupported value for recipe filter" }

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
            val outputPredicate = BiPredicate<Identifier, JsonElement> { _, json ->
                val obj = json.asJsonObject
                if (!obj.has("result")) {
                    return@BiPredicate false
                }
                val resultElement = obj.get("result")
                val resultString = if (resultElement.isJsonObject) {
                    resultElement.asJsonObject.get("item").asString
                } else {
                    resultElement.asString
                }
                if (output.startsWith('#')) {
                    if (tag == null) {
                        tag = Main.resourceManager.registryTagManager.getTag(
                            Registry.ITEM_KEY,
                            Identifier(output.substring(1))
                        ) {
                            NullPointerException(it.toString())
                        }
                    }
                    return@BiPredicate tag!!.contains(Registry.ITEM.get(Identifier(resultString)))
                }
                resultString == output
            }
            predicate = mergePredicates(predicate, outputPredicate)
        }
        return predicate
    }

    @Export
    fun removeAll() {
        removeAll = true
    }

    @Export
    fun remove(value: Value) {
        require(value.hasMembers()) { "Unsupported value for recipe filter" }

        removalPredicates += requireNotNull(convertValueToRecipeFilter(value)) { "Unsupported recipe filter $value" }
    }

    private fun mergePredicates(
        predicate: BiPredicate<Identifier, JsonElement>?,
        newPredicate: BiPredicate<Identifier, JsonElement>
    ): BiPredicate<Identifier, JsonElement> {
        return predicate?.and(newPredicate) ?: newPredicate
    }
}
