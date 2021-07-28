package org.sandboxpowered.fabric.scripting.polyglot

import org.graalvm.polyglot.HostAccess
import org.graalvm.polyglot.Value
import org.sandboxpowered.fabric.scripting.PolyglotScriptLoader
import java.util.function.Consumer

class SandboxResourcePolyglotContext(val resource: String, val scriptLoader: PolyglotScriptLoader) {
    val events = HashMap<String, ArrayList<Consumer<Array<Any>>>>()

    fun registerNetEvent(string: String) {
        scriptLoader.markEventAsNetCapable(string)
    }

    fun emit(event: String, vararg args: Any) {
        scriptLoader.emitEventToAll(event, args)
    }

    fun emitServer(event: String, vararg args: Any) {
        emit(event, "client", args)
    }

    fun emitClient(client: Any, event: String, vararg args: Any) {
        emit(event, args)
    }

    fun on(event: String, function: Value) {
        if (!function.canExecute()) throw UnsupportedOperationException("what")

        if (!events.containsKey(event)) {
            events[event] = ArrayList()
        }
        events[event]?.add(Consumer {
            function.executeVoid(*it)
        })
    }

    fun loadResourceFile(path: String): String? {
        println("Loading: [$path]")
        return null
    }

    fun saveResourceFile(path: String, data: String): Boolean {
        println("Saving: [$path]")
        return false
    }
}