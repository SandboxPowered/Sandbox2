package org.sandboxpowered.fabric.api

import net.minecraft.state.property.Property
import org.graalvm.polyglot.HostAccess.Export

class PolyglotStateProperty(
    private val property: Property<*>?,
    @Export val name: String,
    @Export val type: String,
    private val extra: Array<*> = emptyArray<Any>()
) {
    //Int Property
    @Export
    fun values(): Array<Int> {
        if (type == "int") return extra as Array<Int>
        throw UnsupportedOperationException("Cannot get minimum value of $type property")
    }
}