package org.sandboxpowered.fabric.loading

import org.graalvm.polyglot.Source
import org.sandboxpowered.fabric.Side
import org.sandboxpowered.fabric.addon.AddonScanner
import org.sandboxpowered.fabric.scripting.PolyglotScriptLoader
import org.sandboxpowered.fabric.util.RegexUtil
import java.lang.UnsupportedOperationException
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.name
import kotlin.io.path.readText

class SandboxLoader {

    val polyglotLoader = PolyglotScriptLoader()

    fun load(side: Side) {
        val addons = AddonScanner.scanDirectory(Path.of("resources"))
        addons.forEach {
            val scripts = arrayListOf<String>()

            if (it.config.contains("scripts")) scripts.addAll(it.config.get<ArrayList<String>>("scripts"))
            if (it.config.contains("${side.side}.scripts")) scripts.addAll(it.config.get<ArrayList<String>>("${side.side}.scripts"))

            val basePath = it.path
            scripts.forEach { script ->
                val regex = RegexUtil.convertGlobToRegex(script)
                val scriptPath = basePath.resolve(script)
                when (val extension = scriptPath.extension) {
                    "js", "py" -> {
                        val sourceBuilder = Source.newBuilder(
                            scriptExtensionToLanguage(extension),
                            scriptPath.readText(StandardCharsets.UTF_8),
                            scriptPath.name
                        )

                        if(extension == "js") sourceBuilder.mimeType("application/javascript+module")

                        polyglotLoader.loadScriptContext(it.path.name, sourceBuilder.build())

                        polyglotLoader.emitEventTo(it.path.name, "onResourceLoad")
                    }
                    "jar" -> {
                        if (side == Side.CLIENT) throw UnsupportedOperationException("Unable to load .jar on client")

                        // TODO
                        throw UnsupportedOperationException(".jar is not supported yet")
                    }
                    "lua" -> {
                        // TODO
                        throw UnsupportedOperationException(".lua is not supported yet")
                    }
                    else -> {
                        throw RuntimeException("Unsupported extension .${extension}")
                    }
                }
            }
        }
    }

    private fun scriptExtensionToLanguage(extension: String): String {
        return when (extension) {
            "py" -> "python"
            "jar" -> "java"
            else -> extension
        }
    }

    fun emitEvent(event: String, vararg args: Any) {
        polyglotLoader.emitEventToAll(event, *args)
    }
}