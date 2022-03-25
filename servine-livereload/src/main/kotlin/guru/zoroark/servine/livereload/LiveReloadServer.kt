package guru.zoroark.servine.livereload

import io.ktor.application.install
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.WebSocketSession
import io.ktor.http.cio.websocket.close
import io.ktor.http.cio.websocket.readText
import io.ktor.http.content.resource
import io.ktor.http.content.static
import io.ktor.routing.routing
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.ApplicationEngineFactory
import io.ktor.server.engine.embeddedServer
import io.ktor.websocket.WebSockets
import io.ktor.websocket.webSocket
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.selects.whileSelect
import org.slf4j.LoggerFactory
import kotlin.time.ExperimentalTime

private const val LiveReloadDefaultPort = 35729
private const val LiveReloadProtocolV7 = "http://livereload.com/protocols/official-7"
public val LiveReloadSnippet: String = """
    <script>document.write('<script src="http://'
    + (location.host || 'localhost').split(':')[0]
    + ':35729/livereload.js?snipver=1"></'
    + 'script>')</script>
""".trimIndent()

public class LiveReloadServer {
    private val logger = LoggerFactory.getLogger("servine.livereload")
    public val reloadBus: EventBus<Unit> = EventBus()

    @OptIn(ExperimentalCoroutinesApi::class)
    public fun <TEngine : ApplicationEngine, TConfiguration : ApplicationEngine.Configuration> createServer(
        engine: ApplicationEngineFactory<TEngine, TConfiguration>,
        port: Int = LiveReloadDefaultPort
    ): TEngine = embeddedServer(engine, port = port) {
        install(WebSockets)
        routing {
            webSocket("/livereload") {
                logger.debug("Sending hello command to newly connected client")
                // Handshake sequence
                if (!handshake())
                    return@webSocket

                reloadBus.withSubscription { subscription ->
                    whileSelect {
                        incoming.onReceiveCatching {
                            val receivedFrame = it.getOrNull()
                            if (receivedFrame == null) {
                                logger.debug("Channel closed", it.exceptionOrNull())
                                false
                            } else {
                                val command = receivedFrame.toLiveReloadMessage() ?: return@onReceiveCatching true
                                logger.debug("Received command $command")
                                // TODO do something with this command?
                                true
                            }
                        }

                        subscription.channel.onReceive {
                            logger.debug("Received event from reload bus, sending reload command")
                            send(LiveReloadMessage.Reload("/").toFrame())
                            true
                        }
                    }
                }
                logger.debug("Session finished")
            }
            static {
                resource("livereload.js", "livereload.js", "guru.zoroark.servine.livereload")
            }
        }
    }

    private fun Frame.toLiveReloadMessage(): LiveReloadMessage? {
        return try {
            if (this is Frame.Text) {
                liveReloadDefaultObjectMapper.readValue(this.readText(), LiveReloadMessage::class.java)
            } else {
                throw LiveReloadParsingException("Cannot handle binary frames")
            }
        } catch (ex: LiveReloadParsingException) {
            logger.debug("Received frame could not be parsed into a live reload message.", ex)
            null
        }
    }

    // TODO Not actually experimental anymore, but kotlin-coroutines transitive dependency from Ktor is not up
    //      to date
    @OptIn(ExperimentalTime::class)
    private suspend fun WebSocketSession.handshake(): Boolean {
        send(LiveReloadMessage.ServerHello(listOf(LiveReloadProtocolV7), "servine-livereload").toFrame())
        // TODO add timeout on hello handshake
        while (true) {
            val received = incoming.receive().toLiveReloadMessage()
            if (received is LiveReloadMessage.ClientHello) {
                return if (LiveReloadProtocolV7 in received.protocols) {
                    logger.debug("Handshake complete, client hello: $received")
                    true
                } else {
                    logger.warn(
                        "Connected LiveReload client does not support protocol " +
                                "$LiveReloadProtocolV7, closing connection " +
                                "(client claims to support ${received.protocols})."
                    )
                    close()
                    false
                }
            } else {
                logger.warn("Received non-hello command from client before hello handshake, ignoring...")
            }
        }
    }
}

