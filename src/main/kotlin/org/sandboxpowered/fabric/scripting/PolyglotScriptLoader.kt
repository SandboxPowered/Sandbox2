package org.sandboxpowered.fabric.scripting

import org.graalvm.polyglot.Context
import org.graalvm.polyglot.HostAccess
import org.graalvm.polyglot.Source
import org.sandboxpowered.fabric.scripting.polyglot.SandboxResourcePolyglotContext
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

class PolyglotScriptLoader {
    private val executor = Executors.newSingleThreadExecutor()
    private val scriptContext = HashMap<String, HashMap<String, Context>>()
    private val polyglotContext = HashMap<String, SandboxResourcePolyglotContext>()

    private fun buildContext(): Context = Context.newBuilder("js", "python")
        .allowExperimentalOptions(true)
        .allowHostAccess(HostAccess.newBuilder(HostAccess.EXPLICIT).allowAccessAnnotatedBy<HostAccess.Export>().build()).build()

    private fun getResourceContextMap(resource: String): HashMap<String, Context> {
        return scriptContext.computeIfAbsent(resource) { HashMap() }
    }

    private fun getPolyglotContext(resource: String): SandboxResourcePolyglotContext {
        return polyglotContext.computeIfAbsent(resource) { SandboxResourcePolyglotContext(it, this) }
    }

    fun emitEventToAll(event: String, vararg args: Any) {
        polyglotContext.forEach { (_, context) ->
            if (context.events.containsKey(event)) {
                context.events[event]?.forEach {
                    it.accept(arrayOf(*args))
                }
            }
        }
    }

    fun emitEventTo(resource: String, event: String, vararg args: Any) {
        if(polyglotContext.containsKey(resource)) {
            val context = polyglotContext[resource]!!
            if (context.events.containsKey(event)) {
                context.events[event]?.forEach {
                    it.accept(arrayOf(*args))
                }
            }
        }
        emitEventToAll(resource, "$resource:$event", args)
    }

    fun loadScriptContext(resource: String, scriptSource: Source) {
        val resourceContextMap = getResourceContextMap(resource)

        val context = buildContext()

        resourceContextMap[scriptSource.name] = context

        val bindings = context.getBindings(scriptSource.language)
        bindings.putMember("sandbox", getPolyglotContext(resource))

        try {
            executor.submit { context.eval(scriptSource) }.get(5, TimeUnit.SECONDS)
        } catch (exception: TimeoutException) {
            println("Script ${scriptSource.name} failed to run in under 5 seconds")
        }
    }

    fun unloadResource(resource: String) {
        emitEventTo(resource, "onResourceUnload")
    }

    fun markEventAsNetCapable(string: String) {

    }
}

private inline fun <reified T : Annotation> HostAccess.Builder.allowAccessAnnotatedBy(): HostAccess.Builder {
    return allowAccessAnnotatedBy(T::class.java)
}
