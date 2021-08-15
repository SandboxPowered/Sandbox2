package org.sandboxpowered.fabric.api.item

import org.graalvm.polyglot.HostAccess.Export

interface PolyItemStack {
    @Export
    fun getItem(): PolyItem

    @Export
    fun getCount(): Int
}