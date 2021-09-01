package guru.zoroark.servine.app

import io.ktor.http.ContentType
import io.ktor.http.content.OutgoingContent
import io.ktor.http.defaultForFile
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.writer
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousFileChannel
import java.nio.channels.CompletionHandler
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Recreation of [io.ktor.http.content.LocalFileContent] strictly using NIO
 */
class PathContent(
    private val path: Path,
    override val contentType: ContentType = ContentType.defaultForFile(path)
) : OutgoingContent.ReadChannelContent() {
    override fun readFrom(): ByteReadChannel = path.readChannelButBetter()
}

private fun Path.readChannelButBetter(
    coroutineContext: CoroutineContext = Dispatchers.IO
): ByteReadChannel {
    val length = Files.size(this)
    val fileChannel =
        AsynchronousFileChannel.open(this, StandardOpenOption.READ)
    return CoroutineScope(coroutineContext)
        .writer(
            CoroutineName("path-reader") + coroutineContext,
            autoFlush = false
        ) {
            fileChannel.use {
                val buffer = ByteBuffer.allocate(1024)
                var position = 0L
                while (true) {
                    if (position == length)
                        break
                    val result = fileChannel.readSuspending(buffer, position)
                    if (result == -1)
                        break
                    position += result
                    buffer.flip()
                    channel.writeFully(buffer)
                    buffer.clear()
                }
            }
        }.channel
}

private suspend fun AsynchronousFileChannel.readSuspending(
    buffer: ByteBuffer,
    pos: Long
): Int = suspendCoroutine { cont ->
    read<Nothing?>(
        buffer, pos, null,
        object : CompletionHandler<Int, Nothing?> {
            override fun completed(result: Int, attachment: Nothing?) {
                cont.resume(result)
            }

            override fun failed(exc: Throwable, attachment: Nothing?) {
                cont.resumeWithException(exc)
            }
        }
    )
}
