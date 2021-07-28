package org.sandboxpowered.fabric.scripting.polyglot

import org.graalvm.polyglot.Value
import java.util.function.Consumer

class SandboxPolyglotContext {
    private val events = HashMap<String, ArrayList<Consumer<Array<Any>>>>()
    private val netEvents = ArrayList<String>()

    fun registerNetEvent(string: String) {
        netEvents.add(string)
    }

    fun emit(event: String, vararg args: Any) {
        if (events.containsKey(event)) {
            events[event]?.forEach {
                it.accept(arrayOf(*args))
            }
        }
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

    fun loadResourceFile(resource: String): String? {
        println("Loading: [$resource]")
        return null
    }

    fun saveResourceFile(resource: String, data: String): Boolean {
        println("Saving: [$resource]")
        return false
    }
}