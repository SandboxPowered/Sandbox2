package org.sandboxpowered.fabric

import org.sandboxpowered.fabric.loading.SandboxLoader

object Main {
    fun startSandboxInternals() {
        val loader = SandboxLoader()
        loader.load()
    }
}