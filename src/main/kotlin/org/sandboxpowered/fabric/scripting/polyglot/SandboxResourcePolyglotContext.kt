package org.sandboxpowered.fabric.scripting.polyglot

import org.graalvm.polyglot.HostAccess.Export
import org.graalvm.polyglot.Value
import org.sandboxpowered.fabric.scripting.PolyglotScriptLoader
import java.util.function.Consumer

class SandboxResourcePolyglotContext(val resource: String, private val scriptLoader: PolyglotScriptLoader) {
    val events = HashMap<String, ArrayList<Consumer<Array<Any>>>>()

    @Export
    fun registerNetEvent(string: String) {
        scriptLoader.markEventAsNetCapable(string)
    }

    @Export
    fun emit(event: String, vararg args: Any) {
        scriptLoader.emitEventToAll(event, args)
    }

    @Export
    fun emitServer(event: String, vararg args: Any) {
        emit(event, "client", args)
    }

    @Export
    fun emitClient(client: Any, event: String, vararg args: Any) {
        emit(event, args)
    }

    @Export
    fun on(event: String, function: Value) {
        if (!function.canExecute()) throw UnsupportedOperationException("what")

        if (!events.containsKey(event)) {
            events[event] = ArrayList()
        }
        events[event]?.add(Consumer {
            function.executeVoid(*it)
        })
    }

    @Export
    fun loadResourceFile(path: String): String? {
        println("Loading: [$path]")
        return null
    }

    @Export
    fun saveResourceFile(path: String, data: String): Boolean {
        println("Saving: [$path]")
        return false
    }
}