package guru.zoroark.servine.livereload

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

@JsonDeserialize(using = LiveReloadMessageDeserializer::class)
public sealed class LiveReloadMessage(public val command: String) {
    // Client -> server
    @JsonDeserialize // Resets the deserializer to the default value
    public data class Url(val url: String) : LiveReloadMessage("url")

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonDeserialize // Resets the deserializer to the default value
    public data class ClientHello(val protocols: List<String>) : LiveReloadMessage("hello")

    // Server -> Client
    public data class ServerHello(val protocols: List<String>, val serverName: String) : LiveReloadMessage("hello")
    public data class Reload(val path: String, val liveCss: Boolean = true) : LiveReloadMessage("reload")
    public data class Alert(val message: String) : LiveReloadMessage("alert")
}