package guru.zoroark.servine.app

import guru.zoroark.servine.livereload.LiveReloadServer
import guru.zoroark.shedinja.environment.InjectionScope
import guru.zoroark.shedinja.environment.invoke
import guru.zoroark.shedinja.extensions.services.SuspendShedinjaService
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.netty.Netty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LiveReloadService(scope: InjectionScope) : SuspendShedinjaService {
    private val config: Config by scope()
    private var lrServer: LiveReloadServer? = null
    private var ktorServer: ApplicationEngine? = null

    override suspend fun start() {
        if (config.liveReload) {
            lrServer = LiveReloadServer()
            ktorServer = lrServer!!.createServer(Netty)
            withContext(Dispatchers.IO) { ktorServer!!.start() }
        }
    }

    override suspend fun stop() {
        withContext(Dispatchers.IO) { ktorServer?.stop(500, 3000) }
    }

    suspend fun triggerReload() {
        lrServer?.reloadBus?.emit(Unit)
    }
}