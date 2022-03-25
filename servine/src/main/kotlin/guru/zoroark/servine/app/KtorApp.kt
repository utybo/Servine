package guru.zoroark.servine.app

import guru.zoroark.servine.livereload.LiveReloadSnippet
import guru.zoroark.servine.redirects.ktor.Redirects
import guru.zoroark.servine.redirects.ktor.from
import guru.zoroark.shedinja.environment.InjectionScope
import guru.zoroark.shedinja.environment.invoke
import guru.zoroark.shedinja.extensions.services.SuspendShedinjaService
import io.ktor.application.ApplicationStopping
import io.ktor.application.EventDefinition
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.http.ContentType
import io.ktor.http.defaultForFile
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.ShutDownUrl
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

class KtorApp(scope: InjectionScope) : SuspendShedinjaService {
    private lateinit var server: ApplicationEngine
    private val config: Config by scope()
    private val liveReload: LiveReloadService by scope()

    override suspend fun start() {
        server = embeddedServer(Netty, port = config.port) {
            install(Redirects) { loadRedirectsFile() }
            routing {
                get("/{segments...}") {
                    val segments = call.parameters.getAll("segments")
                        ?.normalize() ?: listOf()
                    val path = Path.of(config.directory, *segments.toTypedArray()).let {
                        if (Files.isDirectory(it)) it.resolve("index.html")
                        else it
                    }
                    if (Files.isRegularFile(path)) {
                        if (config.liveReload && ContentType.defaultForFile(path).match(ContentType.Text.Html)) {
                            // TODO this needs to be optimized
                            val content = withContext(Dispatchers.IO) { Files.readString(path) }
                            val bodyTagLocation = content.lastIndexOf("</body>").let { it..(it + 7) }
                            val contentWithSnippet = content.replaceRange(bodyTagLocation, "$LiveReloadSnippet</body>")
                            call.respondText(contentWithSnippet, ContentType.Text.Html)
                        } else {
                            call.respond(PathContent(path))
                        }
                    }
                }

                route("_servine") {
                    get("reload") {
                        liveReload.triggerReload()
                        call.respond("OK")
                    }
                }
            }
        }
        withContext(Dispatchers.IO) { server.start() }
    }

    override suspend fun stop() {
        withContext(Dispatchers.IO) { server.stop(300, 3000) }
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

    private fun Redirects.Configuration.loadRedirectsFile() {
        val redirectsPath = Path.of("${config.directory}/_redirects")
        if (Files.isRegularFile(redirectsPath)) from(redirectsPath)
    }
}