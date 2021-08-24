package org.sandboxpowered.fabric

import com.google.gson.JsonElement
import net.minecraft.resource.ServerResourceManager
import net.minecraft.state.property.Properties
import net.minecraft.state.property.Property
import net.minecraft.util.Identifier
import org.sandboxpowered.fabric.api.PolyglotRecipeManager
import org.sandboxpowered.fabric.api.StateManagement
import org.sandboxpowered.fabric.loading.SandboxLoader
import org.sandboxpowered.fabric.loading.WebServer

object Main {
    lateinit var resourceManager: ServerResourceManager

    val loader = SandboxLoader()
    var webServer: WebServer? = null

    fun startSandboxInternals() {

    }

    fun startSandboxIntegratedServer() {
        initSandboxContent()
        loader.load(Side.SERVER)
    }

    fun startSandboxDedicatedServer() {
        initSandboxContent()
        loader.load(Side.SERVER)
        webServer?.stop()
        webServer = WebServer().apply { start() }
    }

    fun startSandboxClient() {
        initSandboxContent()
        loader.load(Side.CLIENT)
    }

    fun shutdownSandbox() {
        webServer?.stop()
    }

    private fun initSandboxContent() {
        val c = Properties::class.java
        c.declaredFields.forEach {
            val prop = it.get(null)
            if (prop is Property<*>) StateManagement.putInternalStateProperty(prop)
        }
    }

    fun handleRecipes(map: MutableMap<Identifier, JsonElement>) {
        val recipeManager = PolyglotRecipeManager(map)
        loader.emitEvent("recipes", recipeManager)
        recipeManager.run()
    }
}