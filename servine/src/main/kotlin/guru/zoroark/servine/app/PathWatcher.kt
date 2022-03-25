package guru.zoroark.servine.app

import guru.zoroark.shedinja.environment.InjectionScope
import guru.zoroark.shedinja.environment.invoke
import guru.zoroark.shedinja.extensions.services.SuspendShedinjaService
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds.*

class PathWatcher(scope: InjectionScope) : SuspendShedinjaService {
    private val logger = LoggerFactory.getLogger("servine.pathwatcher")
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val config: Config by scope()
    private val liveReload: LiveReloadService by scope()

    private suspend fun watch(pathToWatch: Path) = withContext(Dispatchers.IO) {
        val watchService = FileSystems.getDefault().newWatchService()
        pathToWatch.register(watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE)
        while (true) {
            val event = watchService.poll() ?: continue
            logger.debug("Detected changes in the following paths: " + event.pollEvents().map { it.context() as Path })
            liveReload.triggerReload()
            event.reset()
            delay(75)
        }
    }

    override suspend fun start() {
        if (config.liveReload) coroutineScope.launch { watch(Path.of(config.directory)) }
    }

    override suspend fun stop() {
        coroutineScope.cancel()
    }
}