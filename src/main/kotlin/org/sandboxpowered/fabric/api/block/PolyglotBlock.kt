package org.sandboxpowered.fabric.api.block

import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.state.StateManager
import org.graalvm.polyglot.Value
import org.sandboxpowered.fabric.api.PolyglotStateProperty

class PolyglotBlock(settings: Settings, private val possibleStates: List<PolyglotStateProperty>?, value: Value) : Block(settings) {
    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        super.appendProperties(builder)
        hackOverwrite?.forEach {
            if (it.property != null) {
                builder.add(it.property)
            }
        }
    }

    companion object {
        var hackOverwrite: List<PolyglotStateProperty>? = null
    }
}