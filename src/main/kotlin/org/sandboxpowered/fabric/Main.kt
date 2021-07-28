package org.sandboxpowered.fabric

import net.minecraft.server.MinecraftServer
import org.sandboxpowered.fabric.loading.SandboxLoader

object Main {
    fun startSandboxInternals() {
    }

    fun startSandboxServer(minecraftServer: MinecraftServer) {
        val loader = SandboxLoader()
        loader.load(Side.SERVER)
    }
}