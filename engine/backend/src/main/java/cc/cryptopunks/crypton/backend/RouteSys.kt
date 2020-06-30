package cc.cryptopunks.crypton.backend

import cc.cryptopunks.crypton.context.Route
import cc.cryptopunks.crypton.util.typedLog
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class RouteSys :
    Route.Sys,
    CoroutineScope {

    override val coroutineContext: CoroutineContext = SupervisorJob() + Dispatchers.Unconfined
    private val log = typedLog()

    private var backend: Backend? = null

    override fun navigate(route: Route) {
        log.d("Navigate $route")
        backend?.run {
//            when (route) {
//                is Route.Back -> drop()
//                else -> request(route)
//            }
        }
    }

    override suspend fun bind(navigator: Any) {
        log.d("Bind $navigator")
        backend = navigator as Backend
    }
}
