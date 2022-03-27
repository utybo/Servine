package guru.zoroark.servine.app

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import guru.zoroark.servine.livereload.LiveReloadServer
import guru.zoroark.servine.livereload.LiveReloadSnippet
import guru.zoroark.servine.redirects.ktor.Redirects
import guru.zoroark.servine.redirects.ktor.from
import guru.zoroark.shedinja.dsl.put
import guru.zoroark.shedinja.dsl.shedinja
import guru.zoroark.shedinja.extensions.services.services
import guru.zoroark.shedinja.extensions.services.useServices
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.http.ContentType
import io.ktor.http.defaultForFile
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.server.engine.ShutDownUrl
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

class App : CliktCommand(
    name = "servine",
    help = """
        Servine is a static file web server with support for '_redirects' files.

        NOTE: DO NOT USE IN PRODUCTION ENVIRONMENTS. This is intended as a developer server and lacks critical security features.
        """.trimIndent()
) {
    private val directory: String by argument(help = "The directory that should be served.")
    private val port: Int by option("-p", help = "Port onto which the server should be started (8080 by default).")
        .int().default(8080)
    private val liveReload: Boolean by option("-r", help = "Enable LiveReload support, which will automatically reload your browser when you change a file served by Servine.").flag(default = false)

    override fun run() {
        require(port in 0..65535) { "$port is not a valid port number." }
        val logger = LoggerFactory.getLogger("servine.main")

        val env = shedinja {
            useServices()

            put(::KtorApp)
            put(::LiveReloadService)
            put(::PathWatcher)
            put { Config(port = port, liveReload = liveReload, directory = directory) }
        }
        println("Server starting on 127.0.0.1:$port")
        runBlocking(Dispatchers.Default) {
            env.services.startAll(logger::info)
        }
    }
}


fun main(args: Array<String>) {
    App().main(args)
}
