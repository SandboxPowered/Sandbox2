package org.sandboxpowered.fabric.api.block

import net.minecraft.util.Identifier
import org.graalvm.polyglot.HostAccess.Export
import org.graalvm.polyglot.Value

class PolyglotBlockManager(private val domain: String, private val global: PolyglotGlobalBlockManager) {
    @Export
    fun add(id: String, value: Value) {
        require(value.hasMembers()) { "Unsupported value for block registration" }
        global.addBlock(Identifier(domain, id), value)
    }
}