package org.sandboxpowered.fabric.loading

import com.electronwill.nightconfig.core.file.FileConfig
import com.google.common.hash.Hashing
import com.google.gson.Gson
import com.google.gson.JsonObject
import org.apache.commons.io.FileUtils
import org.apache.logging.log4j.LogManager.getLogger
import org.graalvm.polyglot.Source
import org.sandboxpowered.fabric.Side
import org.sandboxpowered.fabric.addon.AddonScanner
import org.sandboxpowered.fabric.scripting.PolyglotScriptLoader
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.*
import com.google.common.io.Files as GoogleFiles

class SandboxLoader {

    val polyglotLoader = PolyglotScriptLoader()
    val resourceContent: MutableMap<String, MutableList<Path>> = hashMapOf()

    private val log = getLogger()

    fun load(side: Side) {
        //TODO: make client side not scan instead load from list obtained from server
        val addons = AddonScanner.scanDirectory(Path.of("resources"))
        val cacheDir = Path.of(".sandbox/cache")
        if (Files.exists(cacheDir))
            FileUtils.deleteDirectory(cacheDir.toFile())
        Files.createDirectories(cacheDir)
        addons.forEach { addon ->
            val scripts = arrayListOf<Path>()

            val commonScripts = addon.config.getPathList(addon.path, "scripts")
            val clientScripts = addon.config.getPathList(addon.path, "client.scripts")
            val serverScripts = addon.config.getPathList(addon.path, "server.scripts")

            scripts.addAll(commonScripts)
            when (side) {
                Side.CLIENT -> scripts.addAll(clientScripts)
                else -> scripts.addAll(serverScripts)
            }

            if (addon.config.contains("files")) {
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

                        if (extension == "js") sourceBuilder.mimeType("application/javascript+module")

                        polyglotLoader.loadScriptContext(addon.path.name, sourceBuilder.build())
                    }
                    "jar" -> {
                        require(side != Side.CLIENT) { "Unable to load .jar on client" }

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

        log.info("Loaded ${addons.size} resources")

        if (side == Side.SERVER) {
            val json = JsonObject()

            resourceContent.forEach { (resource, files) ->
                val resourceJson = JsonObject()
                val resourceHash = Hashing.md5().hashString(resource, StandardCharsets.UTF_8).toString()
                val resourceCachePath = cacheDir.resolve(resourceHash)
                if (Files.notExists(resourceCachePath))
                    Files.createDirectories(resourceCachePath)
                resourceJson.addProperty("_domain", resource)
                files.forEach {
                    val hash = GoogleFiles.hash(it.asFile, Hashing.md5()).toString()
                    it.copyTo(resourceCachePath.resolve(hash), true)
                    resourceJson.addProperty(hash, it.invariantSeparatorsPathString)
                }
                json.add(resourceHash, resourceJson)
            }

            cacheDir.resolve("manifest.json").writeText(Gson().toJson(json), StandardCharsets.UTF_8)
            log.info("Updated client resource cache")
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