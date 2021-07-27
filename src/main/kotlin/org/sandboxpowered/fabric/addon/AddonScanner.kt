package org.sandboxpowered.fabric.addon

import com.electronwill.nightconfig.core.file.FileConfig
import com.electronwill.nightconfig.toml.TomlFormat
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.name

object AddonScanner {
    fun scanDirectory(path: Path): List<AddonReference> {
        val addons = ArrayList<AddonReference>()
        Files.list(path).forEach {
            if (Files.isDirectory(it)) {
                val name = it.name
                if (name.startsWith('[') && name.endsWith(']')) {
                    addons.addAll(scanDirectory(it))
                } else {
                    val manifestPath = it.resolve("manifest.toml")
                    if (Files.exists(manifestPath)) {
                        val manifest = FileConfig.of(manifestPath, TomlFormat.instance())
                        manifest.load()
                        addons.add(AddonReference(it, manifest))
                    }
                }
            }
        }
        return addons
    }
}