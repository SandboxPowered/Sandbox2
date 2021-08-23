package org.sandboxpowered.fabric.api.block

import com.google.common.collect.ImmutableMap
import net.minecraft.block.AbstractBlock
import net.minecraft.block.Block
import net.minecraft.block.Material
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import org.graalvm.polyglot.Value
import org.sandboxpowered.fabric.api.PolyglotStateProperty
import org.sandboxpowered.fabric.util.*

class PolyglotGlobalBlockManager {
    private val archetypeMap: Map<String, (Value) -> Block> = ImmutableMap.builder<String, (Value) -> Block>()
        .apply {
            this["block"] = {
                val settings: AbstractBlock.Settings
                var possibleStates: List<PolyglotStateProperty>? = null
                if (!it.hasMember("properties")) settings = AbstractBlock.Settings.of(Material.AIR)
                else {
                    val properties = it.getMember("properties")
                    settings = itemPropertiesToSettings(properties)
                    possibleStates = properties.getMemberValue("state")?.let { state -> state.asArray().map { value -> value.convert() } }
                }
                PolyglotBlock.hackOverwrite = possibleStates
                PolyglotBlock(settings, possibleStates ?: emptyList(), it)
            }
        }
        .build()

    private fun itemPropertiesToSettings(properties: Value): AbstractBlock.Settings {
        val settings = AbstractBlock.Settings.of(materialFromString(properties.getMemberValue("material", "air")))
        properties.getMemberValueFloat("hardness")?.let(settings::hardness)
        properties.getMemberValueFloat("resistance")?.let(settings::resistance)
        return settings
    }

    private fun materialFromString(input: String): Material {
        return when (input) {
            "stone" -> Material.STONE
            "ice" -> Material.ICE
            "soil", "dirt" -> Material.SOIL
            else -> Material.AIR
        }
    }

    fun addBlock(id: Identifier, value: Value) {
        val archetype = value.getMemberValue("archetype", "block")
        require(archetype in archetypeMap) { "Unknown block archetype [$archetype]" }
        val block = archetypeMap[archetype]!!(value)
        Registry.register(Registry.BLOCK, id, block)
        //TODO register itemblock
    }
}