package guru.zoroark.servine.app

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import guru.zoroark.servine.redirects.ktor.Redirects
import guru.zoroark.servine.redirects.ktor.from
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.files
import io.ktor.http.content.static
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.util.normalizePathComponents
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import java.util.*

class App : CliktCommand() {
    private val directory: String by argument()
    // private val defaultFile: String by option().default("index.html")

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
