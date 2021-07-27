package org.sandboxpowered.fabric.scripting.js

import org.graalvm.polyglot.Value

class SandboxJS {
    fun registerNetEvent(string: String) {
        println("RegisterNetEvent: [$string]")
    }

    fun emit(event: String, vararg args: Any) {

    }

    fun emitServer(event: String, vararg args: Any) {

    }

    fun emitClient(client: Any, event: String, vararg args: Any) {

    }

    fun on(event: String, function: Value) {
        if (!function.canExecute()) throw UnsupportedOperationException("what")

        println("SubscribedEvent: [$event]")
    }

    fun loadResourceFile(resource: String): String? {
        println("Loading: [$resource]")
        return null
    }

    fun saveResourceFile(resource: String, data: String): Boolean {
        println("Saving: [$resource]")
        return false
    }
}