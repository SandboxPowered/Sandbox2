package org.sandboxpowered.fabric.loading

import org.sandboxpowered.fabric.addon.AddonScanner
import org.sandboxpowered.fabric.scripting.JSScriptLoader
import org.sandboxpowered.fabric.util.RegexUtil
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.util.ArrayList
import kotlin.io.path.name
import kotlin.io.path.readText

class SandboxLoader {
    fun load() {
        val jsLoader = JSScriptLoader()
        jsLoader.init()
        val addons = AddonScanner.scanDirectory(Path.of("resources"))
        addons.forEach {
            val scripts = arrayListOf<String>()
            if(it.config.contains("scripts")) scripts.addAll(it.config.get<ArrayList<String>>("scripts"))
            if(it.config.contains("server.scripts")) scripts.addAll(it.config.get<ArrayList<String>>("server.scripts"))

            val basePath = it.path
            scripts.forEach { script ->
                val regex = RegexUtil.convertGlobToRegex(script)
                val scriptPath = basePath.resolve(script)
                jsLoader.eval(scriptPath.readText(StandardCharsets.UTF_8), scriptPath.name)
            }
        }
    }
}

fun <E> ArrayList<E>.addAll(vararg elements: E) {
    elements.forEach(this::add)
}
