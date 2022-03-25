package guru.zoroark.servine.livereload

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.TextNode
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.ktor.http.cio.websocket.Frame

public class LiveReloadMessageDeserializer : StdDeserializer<LiveReloadMessage>(LiveReloadMessage::class.java) {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): LiveReloadMessage {
        val node: TreeNode = p.codec.readTree(p)
        val commandNode = node.get("command") ?: throw LiveReloadParsingException("Field 'command' not found.")
        val command = (commandNode as? TextNode)?.textValue()
            ?: throw LiveReloadParsingException("Field 'command' is not a string.")
        return when (command) {
            "hello" -> p.codec.treeToValue(node, LiveReloadMessage.ClientHello::class.java)
            "url" -> p.codec.treeToValue(node, LiveReloadMessage.Url::class.java)
            else -> throw LiveReloadParsingException("Unknown command '$command'")
        }
    }
}

public val liveReloadDefaultObjectMapper: ObjectMapper = ObjectMapper().registerKotlinModule()

public fun LiveReloadMessage.toFrame(): Frame = Frame.Text(liveReloadDefaultObjectMapper.writeValueAsString(this))