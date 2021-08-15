package org.sandboxpowered.fabric.api.block

import org.graalvm.polyglot.HostAccess.Export

interface PolyBlock {
    @Export
    fun id(): String
}