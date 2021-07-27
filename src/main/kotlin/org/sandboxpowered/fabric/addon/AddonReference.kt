package org.sandboxpowered.fabric.addon

import com.electronwill.nightconfig.core.file.FileConfig
import java.nio.file.Path

data class AddonReference(val path: Path, val config: FileConfig)