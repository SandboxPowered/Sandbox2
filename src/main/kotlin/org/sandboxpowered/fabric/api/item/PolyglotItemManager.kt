package org.sandboxpowered.fabric.api.item

import net.minecraft.util.Identifier
import org.graalvm.polyglot.HostAccess.Export
import org.graalvm.polyglot.Value

class PolyglotItemManager(private val domain: String, private val global: PolyglotGlobalItemManager) {
    @Export
    fun add(id: String, value: Value) {
        require(value.hasMembers()) { "Unsupported value as custom item" }
        global.addItem(Identifier(domain, id), value)
    }
}