package org.sandboxpowered.fabric.loading

import com.electronwill.nightconfig.core.file.FileConfig
import org.graalvm.polyglot.Source
import org.sandboxpowered.fabric.Side
import org.sandboxpowered.fabric.addon.AddonScanner
import org.sandboxpowered.fabric.scripting.PolyglotScriptLoader
import org.sandboxpowered.fabric.util.RegexUtil
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.name
import kotlin.io.path.readText

class SandboxLoader {

    val polyglotLoader = PolyglotScriptLoader()
    val resourceContent: MutableMap<String, MutableList<Path>> = hashMapOf()

    fun load(side: Side) {
        val addons = AddonScanner.scanDirectory(Path.of("resources"))
        addons.forEach { addon ->
            val scripts = arrayListOf<Path>()

            val commonScripts = addon.config.getPathList(addon.path,"scripts")
            val clientScripts = addon.config.getPathList(addon.path,"client.scripts")
            val serverScripts = addon.config.getPathList(addon.path,"server.scripts")

            scripts.addAll(commonScripts)
            when (side) {
                Side.CLIENT -> scripts.addAll(clientScripts)
                else -> scripts.addAll(serverScripts)
            }

            if(addon.config.contains("files")) {
                val resourceGlobs = addon.config.get<ArrayList<String>>("files")
                val filter = SandboxFileVisitor(addon.path, resourceGlobs)
                Files.walkFileTree(addon.path, filter)

                filter.output.addAll(commonScripts)
                filter.output.addAll(clientScripts)

                resourceContent[addon.path.name] = filter.output
            }

            scripts.forEach { scriptPath ->
                when (val extension = scriptPath.extension) {
                    "js", "py" -> {
                        val sourceBuilder = Source.newBuilder(
                            scriptExtensionToLanguage(extension),
                            scriptPath.readText(StandardCharsets.UTF_8),
                            scriptPath.name
                        )

                        if(extension == "js") sourceBuilder.mimeType("application/javascript+module")

                        polyglotLoader.loadScriptContext(addon.path.name, sourceBuilder.build())
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

            polyglotLoader.emitEventTo(addon.path.name, "onResourceLoad")
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

private fun FileConfig.getPathList(base: Path, s: String): List<Path> {
    if (contains(s))
        return get<List<String>>(s).map { base.resolve(it) }
    return emptyList()
}

private val Path.asFile: File
    get() = toFile()