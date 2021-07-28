package org.sandboxpowered.fabric.scripting.polyglot

import com.google.gson.JsonElement
import net.minecraft.util.Identifier
import org.graalvm.polyglot.HostAccess.Export
import org.graalvm.polyglot.Value

class PolyglotRecipeManager(map: MutableMap<Identifier, JsonElement>) {
    @Export
    fun remove(value: Value) {
        println(value)
    }
}