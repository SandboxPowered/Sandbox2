package org.sandboxpowered.fabric.loading

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.apache.logging.log4j.LogManager.getLogger
import team.yi.ktor.features.banner
import java.util.concurrent.TimeUnit
import kotlin.io.path.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.notExists

class WebServer {
    private val globalPath = Path(".sandbox/cache/").toAbsolutePath()
    private var server: NettyApplicationEngine? = null
    fun start() {
        val log = getLogger()
        server = embeddedServer(Netty, port = 25566) {
            banner {
                bannerText = "Sandbox 2"

                render {
                    it.text.split('\n').forEach(log::info)
                }
            }

            routing {
                get("/") {
                    call.respond("Sandbox2 Content Server.")
                }
                get("/assets/{path...}") {
                    val params = call.parameters.getAll("path") ?: emptyList()
                    if (params.isEmpty()) {
                        call.respond(HttpStatusCode.NotFound, "Unknown Path")
                    } else {
                        val cachePath = globalPath.resolve(params.joinToString("/")).normalize()

                        when {
                            !cachePath.startsWith(globalPath) -> call.respond(
                                HttpStatusCode.Forbidden,
                                "Invalid Access"
                            )
                            cachePath.notExists() or cachePath.isDirectory() -> call.respond(HttpStatusCode.NotFound,
                                "Unknown Path")
                            else -> call.respondFile(cachePath.toFile())
                        }
                    }
                }
            }
        }.start(false)
    }

    fun stop() {
        server?.stop(10, 10, TimeUnit.SECONDS)
    }
}