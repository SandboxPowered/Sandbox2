package org.sandboxpowered.fabric.scripting

import org.graalvm.polyglot.Context
import org.graalvm.polyglot.HostAccess
import org.graalvm.polyglot.Source
import org.sandboxpowered.fabric.scripting.js.SandboxFileSystem
import org.sandboxpowered.fabric.scripting.js.SandboxJS
import java.util.function.Function

class JSScriptLoader {
    private val scriptContext = HashMap<String, HashMap<String, Context>>()
    val sbxJS = SandboxJS()
    private val loadModuleFunction: Function<String, Any> = Function {
        when (it) {
            "core" -> sbxJS
            else -> throw RuntimeException("Unknown module '$it'")
        }
    }

    fun buildContext(): Context = Context.newBuilder("js")
        .allowExperimentalOptions(true)
        .fileSystem(SandboxFileSystem())
        .allowHostAccess(HostAccess.newBuilder(HostAccess.EXPLICIT).allowPublicAccess(true).build())
        .allowIO(true)
        .build()

    private fun getResourceContextMap(resource: String): HashMap<String, Context> {
        return scriptContext.computeIfAbsent(resource) { HashMap() }
    }

    fun loadScriptContext(resource: String, scriptSource: Source) {
        val resourceContextMap = getResourceContextMap(resource)

        val context = buildContext();

        resourceContextMap[scriptSource.name] = context

        val bindings = context.getBindings("js")

        bindings.putMember("loadModule", loadModuleFunction)

        context.eval(scriptSource)
    }
}