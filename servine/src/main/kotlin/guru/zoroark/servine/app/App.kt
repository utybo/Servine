package guru.zoroark.servine.app

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.default
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import guru.zoroark.servine.redirects.ktor.Redirects
import guru.zoroark.servine.redirects.ktor.from
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import java.nio.file.Files
import java.nio.file.Path
import java.util.LinkedList

class App : CliktCommand(
    name = "servine",
    help = """
        Servine is a static file web server with support for '_redirects' files.

        NOTE: DO NOT USE IN PRODUCTION ENVIRONMENTS. This is intended as a developer server and lacks critical security features.
        """.trimIndent()
) {
    private val directory: String by argument(help = "The directory that should be served.")
    private val port: Int by option("-p", help = "Port onto which the server should be started")
        .int().default(8080)

    override fun run() {
        require(port in 0..65535) { "$port is not a valid port number." }
        val server = embeddedServer(Netty, port = port) {
            install(Redirects) { loadRedirectsFile() }

            routing {
                get("/{segments...}") {
                    val segments = call.parameters.getAll("segments")
                        ?.normalize() ?: listOf()
                    val path = Path.of(directory, *segments.toTypedArray()).let {
                        if (Files.isDirectory(it)) it.resolve("index.html")
                        else it
                    }
                    if (Files.isRegularFile(path))
                        call.respond(PathContent(path))
                }
            }
        }
        println("Server starting on 127.0.0.1:$port")
        server.start(wait = true)
    }

    private fun Redirects.Configuration.loadRedirectsFile() {
        val redirectsPath = Path.of("$directory/_redirects")
        if (Files.isRegularFile(redirectsPath)) from(redirectsPath)
    }
}

private fun List<String>.normalize(): List<String> {
    val stack = LinkedList<String>()
    for (segment in this) {
        when {
            segment.isEmpty() || segment == "." -> {
                // Do nothing
            }
            segment == ".." -> {
                if (stack.isNotEmpty()) stack.pop()
            }
            else -> {
                stack.push(segment)
            }
        }
    }
    return stack.reversed()
}

fun main(args: Array<String>) {
    App().main(args)
}
