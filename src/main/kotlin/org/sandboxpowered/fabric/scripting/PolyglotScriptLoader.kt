package org.sandboxpowered.fabric.scripting

import org.graalvm.polyglot.Context
import org.graalvm.polyglot.HostAccess
import org.graalvm.polyglot.Source
import org.sandboxpowered.fabric.api.SandboxResourcePolyglotContext
import org.sandboxpowered.fabric.scripting.polyglot.PolyglotFileSystem
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

class PolyglotScriptLoader {
    private val executor = Executors.newSingleThreadExecutor()
    private val scriptContext: MutableMap<String, MutableMap<String, Context>> = HashMap()
    private val polyglotContext: MutableMap<String, SandboxResourcePolyglotContext> = HashMap()

    private fun buildContext(): Context = Context.newBuilder("js", "python")
        .allowExperimentalOptions(true)
        .fileSystem(PolyglotFileSystem())
        .allowIO(true)
        .allowHostAccess(HostAccess.EXPLICIT).build()

    private fun getResourceContextMap(resource: String): MutableMap<String, Context> {
        return scriptContext.computeIfAbsent(resource) { HashMap() }
    }

    private fun getPolyglotContext(resource: String): SandboxResourcePolyglotContext {
        return polyglotContext.computeIfAbsent(resource) { SandboxResourcePolyglotContext(it, this) }
    }

    fun emitEventToAll(event: String, vararg args: Any) {
        polyglotContext.values.forEach { context ->
            context.event(event) { it(args) }
        }
    }

    fun emitEventTo(resource: String, event: String, vararg args: Any) {
        polyglotContext[resource]?.event(event) { it(args) }
        emitEventToAll(resource, "$resource:$event", *args)
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
            error("Script ${scriptSource.name} failed to run in under 5 seconds")
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    fun unloadResource(resource: String) {
        emitEventTo(resource, "onResourceUnload")

        polyglotContext.remove(resource)
        scriptContext.remove(resource)
    }

    fun markEventAsNetCapable(string: String) {

    }
}

private inline fun <reified T : Annotation> HostAccess.Builder.allowAccessAnnotatedBy(): HostAccess.Builder {
    return allowAccessAnnotatedBy(T::class.java)
}
