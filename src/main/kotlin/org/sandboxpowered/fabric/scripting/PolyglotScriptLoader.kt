package org.sandboxpowered.fabric.scripting

import org.graalvm.polyglot.Context
import org.graalvm.polyglot.HostAccess
import org.graalvm.polyglot.Source
import org.sandboxpowered.fabric.scripting.polyglot.SandboxFileSystem
import org.sandboxpowered.fabric.scripting.polyglot.SandboxPolyglotContext
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.function.Function


class PolyglotScriptLoader {
    private val executor = Executors.newSingleThreadExecutor()
    private val scriptContext = HashMap<String, HashMap<String, Context>>()
    val polyglotContext = SandboxPolyglotContext()

    private fun buildContext(): Context = Context.newBuilder("js", "python")
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

        val context = buildContext()

        resourceContextMap[scriptSource.name] = context

        val bindings = context.getBindings(scriptSource.language)

        bindings.putMember("sandbox", polyglotContext)

        try {
            executor.submit { context.eval(scriptSource) }.get(5, TimeUnit.SECONDS)
        } catch (exception: TimeoutException) {
            println("Script ${scriptSource.name} failed to run in under 5 seconds")
        }
    }
}