package guru.zoroark.servine.livereload

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collect
import org.slf4j.LoggerFactory

/**
 * An event bus specifically designed for receiving short events.
 *
 * This is an *unreliable* event bus where messages may be dropped, left unconsumed and/or not replayed.
 */
public class EventBus<T> {
    private val logger = LoggerFactory.getLogger("servine.livereload.eventbus")
    private val flow = MutableSharedFlow<T>(0)

    public suspend fun emit(message: T) {
        flow.emit(message)
    }

    public fun subscribe(inScope: CoroutineScope): EventBusSubscription<T> {
        return EventBusSubscription(flow, inScope)
    }
}

public suspend fun <T, R> EventBus<T>.withSubscription(block: suspend CoroutineScope.(EventBusSubscription<T>) -> R): R {
    return coroutineScope {
        val subscription = subscribe(this)
        try {
            block(subscription)
        } finally {
            subscription.unsubscribe()
        }
    }
}

public class EventBusSubscription<T>(flow: SharedFlow<T>, scope: CoroutineScope) {
    private val fullChannel = Channel<T>()
    public val channel: ReceiveChannel<T> = fullChannel
    private val monitorCoroutine = scope.launch {
        flow.collect {
            try {
                fullChannel.send(it)
            } catch (ex: ClosedSendChannelException) {
                cancel()
            }
        }
    }

    public fun unsubscribe() {
        monitorCoroutine.cancel()
    }
}

