package org.sandboxpowered.fabric.scripting

import org.graalvm.polyglot.Context
import org.graalvm.polyglot.HostAccess
import org.graalvm.polyglot.Source
import org.sandboxpowered.fabric.scripting.js.SandboxFileSystem
import org.sandboxpowered.fabric.scripting.js.SandboxJS
import java.util.function.Function

class JSScriptLoader {
    private val context: Context = Context.newBuilder("js")
        .allowExperimentalOptions(true)
        .fileSystem(SandboxFileSystem())
        .allowHostAccess(HostAccess.newBuilder(HostAccess.EXPLICIT).allowPublicAccess(true).build())
        .allowIO(true)
        .build()
    private val sbxJS = SandboxJS()
    private val loadModuleFunction: Function<String, Any> = Function {
        when (it) {
            "core" -> sbxJS
            else -> throw RuntimeException("Unknown module '$it'")
        }
    }

    fun init() {
        val bindings = context.getBindings("js")

        bindings.putMember("loadModule", loadModuleFunction)
    }

    fun eval(script: String, fileName: String) {
        context.eval(Source.newBuilder("js", script, fileName).build())
    }
}