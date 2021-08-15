package org.sandboxpowered.fabric.api.item

import org.graalvm.polyglot.HostAccess.Export

interface PolyItem {
    @Export
    fun id(): String
}