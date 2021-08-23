package org.sandboxpowered.fabric.api

import org.graalvm.polyglot.HostAccess.Export
import org.graalvm.polyglot.Value
import org.sandboxpowered.fabric.scripting.PolyglotScriptLoader

typealias Consumer = (Array<out Any>) -> Unit

class SandboxResourcePolyglotContext(private val resource: String, private val scriptLoader: PolyglotScriptLoader) {
    private val events: MutableMap<String, MutableList<Consumer>> = HashMap()

    operator fun get(eventName: String): List<Consumer>? = events[eventName]

    inline fun event(name: String, body: (Consumer) -> Unit) {
        this[name]?.forEach(body)
    }

    @Export
    fun registerNetEvent(string: String) {
        scriptLoader.markEventAsNetCapable(string)
    }

    @Export
    fun emit(event: String, vararg args: Any) {
        if (event.contains(':')) scriptLoader.emitEventToAll(event, *args)
        else scriptLoader.emitEventTo(resource, event, *args)
    }

    @Export
    fun emitServer(event: String, vararg args: Any) {
        emit(event, "client", *args)
    }

    @Export
    fun emitClient(client: Any, event: String, vararg args: Any) {
        emit(event, *args)
    }

    @Export
    fun on(event: String, function: Value) {
        require(function.canExecute()) { "Unable to register non-function to event $event" }

        if (!events.containsKey(event)) events[event] = ArrayList()

        events.computeIfAbsent(event) { mutableListOf() }.add(function::execute)
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

    @Export
    fun getStateProperty(name: String, type: String, vararg extra: Value): PolyglotStateProperty {
        return StateManagement.getStateProperty(name, type, *extra)
    }
}