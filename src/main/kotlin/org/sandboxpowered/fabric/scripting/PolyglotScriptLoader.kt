package org.sandboxpowered.fabric.scripting

import org.graalvm.polyglot.Context
import org.graalvm.polyglot.HostAccess
import org.graalvm.polyglot.Source
import org.sandboxpowered.fabric.api.SandboxResourcePolyglotContext
import org.sandboxpowered.fabric.api.item.PolyglotGlobalItemManager
import org.sandboxpowered.fabric.api.item.PolyglotItemManager
import org.sandboxpowered.fabric.scripting.polyglot.PolyglotFileSystem
import org.sandboxpowered.fabric.util.TimingUtil
import java.util.concurrent.Executors

class PolyglotScriptLoader {
    private val executor = Executors.newSingleThreadExecutor()
    private val scriptContext: MutableMap<String, MutableMap<String, Context>> = hashMapOf()
    private val polyglotContext: MutableMap<String, SandboxResourcePolyglotContext> = hashMapOf()
    private val globalItemManager = PolyglotGlobalItemManager()
    private val itemManager: MutableMap<String, PolyglotItemManager> = hashMapOf()

    private fun buildContext(): Context = Context.newBuilder("js", "python")
        .allowExperimentalOptions(true)
        .fileSystem(PolyglotFileSystem())
        .allowIO(true)
        .allowHostAccess(HostAccess.EXPLICIT).build()

    private fun getResourceContextMap(resource: String): MutableMap<String, Context> {
        return scriptContext.computeIfAbsent(resource) { HashMap() }
    }

    private fun getPolyglotContext(resource: String): SandboxResourcePolyglotContext =
        polyglotContext.computeIfAbsent(resource) { SandboxResourcePolyglotContext(it, this) }

    fun getItemManager(resource: String): PolyglotItemManager =
        itemManager.computeIfAbsent(resource) { PolyglotItemManager(it, globalItemManager) }

    fun emitEventToAll(event: String, vararg args: Any) {
        polyglotContext.values.forEach { context ->
            context.event(event) { it(args) }
        }
    }

    fun emitEventTo(resource: String, event: String, emitToAll: Boolean = true, vararg args: Any) {
        polyglotContext[resource]?.event(event) { it(args) }
        if (emitToAll)
            emitEventToAll(resource, "$resource:$event", *args)
    }

    fun loadScriptContext(resource: String, scriptSource: Source) {
        val resourceContextMap = getResourceContextMap(resource)

        val context = buildContext()

        resourceContextMap[scriptSource.name] = context

        val bindings = context.getBindings(scriptSource.language)
        bindings.putMember("sandbox", getPolyglotContext(resource))

        val result = TimingUtil.execute(func = { context.eval(scriptSource) }, executor = executor)

        if (result is TimingUtil.Timeout) {
            println("Script $resource:${scriptSource.name} failed to run in under 5 seconds")
            //TODO: cancel and unload this script's resource due to failure, throw error if addon is deemed required
        } else if (result is TimingUtil.Error) {
            throw RuntimeException("Encountered unknown error when executing script $resource:${scriptSource.name}",
                result.error)
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
