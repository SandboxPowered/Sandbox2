package org.sandboxpowered.fabric.util

import com.mojang.bridge.game.GameVersion
import org.sandboxpowered.api.engine.MinecraftVersion

class MinecraftVersion(private val version: GameVersion) : MinecraftVersion {
    override fun compareTo(other: MinecraftVersion?): Int {
        if (this == other)
            return 0
        return 0
    }

    override fun getName(): String = version.name

    override fun isAtLeast(version: MinecraftVersion): Boolean {
        TODO("Not yet implemented")
    }

    override fun isPatchVersionOf(version: MinecraftVersion): Boolean {
        TODO("Not yet implemented")
    }

    override fun isSnapshot(): Boolean = !version.isStable

    override fun isPreRelease(): Boolean = version.name.contains("-pre")
}