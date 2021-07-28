package org.sandboxpowered.fabric.scripting.polyglot

import org.graalvm.polyglot.HostAccess.Export

interface PolyItemStack {
    @Export
    fun getItem(): PolyItem

    @Export
    fun getCount(): Int
}