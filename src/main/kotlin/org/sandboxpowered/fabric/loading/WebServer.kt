package org.sandboxpowered.fabric.loading

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.apache.logging.log4j.LogManager.getLogger
import team.yi.kfiglet.FigFont
import team.yi.ktor.features.banner

class WebServer {
    fun start() {
        val log = getLogger()
        embeddedServer(Netty, port = 25566) {
            banner {
                bannerText = "Sandbox 2"

                render {
                    it.text.split('\n').forEach(log::info)
                }
            }

            routing {
                get("/") {
                    call.respond("hello")
                }
            }
        }.start(false)
    }
}