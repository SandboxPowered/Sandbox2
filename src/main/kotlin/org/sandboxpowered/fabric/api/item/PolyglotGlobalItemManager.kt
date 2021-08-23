package org.sandboxpowered.fabric.api.item

import com.google.common.collect.ImmutableMap
import net.minecraft.item.Item
import net.minecraft.util.Identifier
import net.minecraft.util.Rarity
import net.minecraft.util.registry.Registry
import org.graalvm.polyglot.Value
import org.sandboxpowered.fabric.impl.item.PolyglotItem
import org.sandboxpowered.fabric.util.getMemberValue
import org.sandboxpowered.fabric.util.getMemberValueInt
import org.sandboxpowered.fabric.util.getMemberValueStr
import org.sandboxpowered.fabric.util.set

class PolyglotGlobalItemManager {
    private val archetypeMap: Map<String, (Value) -> Item> = ImmutableMap.builder<String, (Value) -> Item>().apply {
        this["item"] = { PolyglotItem(itemPropertiesToSettings(it), it) }
    }.build()

    private fun itemPropertiesToSettings(value: Value): Item.Settings {
        val settings = Item.Settings()
        if (!value.hasMember("properties")) return settings
        val properties = value.getMember("properties")
        properties.getMemberValueInt("maxDamage")?.let(settings::maxDamage)
        properties.getMemberValueInt("maxCount")?.let(settings::maxCount)
        properties.getMemberValueStr("rarity")?.let {
            settings.rarity(stringToRarity(it))
        }
        return settings
    }

    private fun stringToRarity(rarity: String): Rarity = when (rarity) {
        "uncommon" -> Rarity.UNCOMMON
        "rare" -> Rarity.RARE
        "epic" -> Rarity.EPIC
        else -> Rarity.COMMON
    }

    fun addItem(identifier: Identifier, value: Value) {
        val archetype = value.getMemberValue("archetype", "item")
        if (archetype !in archetypeMap) throw UnsupportedOperationException("Unknown item archetype [$archetype]")
        val item = archetypeMap[archetype]!!(value)
        Registry.register(Registry.ITEM, identifier, item)
    }
}
