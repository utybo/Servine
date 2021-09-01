package guru.zoroark.servine.app

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import guru.zoroark.servine.redirects.ktor.Redirects
import guru.zoroark.servine.redirects.ktor.from
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
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

    override fun run() {
        val server = embeddedServer(Netty, port = 8080) {
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
        println("Server started on 127.0.0.1:8080")
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
    return stack.toList()
}

fun main(args: Array<String>) {
    App().main(args)
}
