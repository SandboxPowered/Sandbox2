package org.sandboxpowered.fabric

import com.google.gson.JsonElement
import net.minecraft.resource.ServerResourceManager
import net.minecraft.util.Identifier
import org.sandboxpowered.fabric.loading.SandboxLoader
import org.sandboxpowered.fabric.scripting.polyglot.PolyglotRecipeManager

object Main {
    lateinit var resourceManager: ServerResourceManager

    val loader = SandboxLoader()

    fun startSandboxInternals() {
    }

    fun startSandboxServer() {
        loader.load(Side.SERVER)
    }

    fun handleRecipes(map: MutableMap<Identifier, JsonElement>) {
        val recipeManager = PolyglotRecipeManager(map)
        loader.emitEvent("recipes", recipeManager)
        recipeManager.run()
    }
}