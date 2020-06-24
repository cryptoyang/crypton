package cc.cryptopunks.crypton.interactor

import cc.cryptopunks.crypton.context.AppScope
import cc.cryptopunks.crypton.context.SessionScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

internal fun AppScope.reconnectSessions(): List<Job> =
    sessionStore.get().values.map { session ->
        launch { session.reconnectIfNeeded() }
    }

private suspend fun SessionScope.reconnectIfNeeded() {
    log.d("reconnecting: $address")
    if (isConnected()) interrupt()
    connect()
    if (!isAuthenticated()) login()
    initOmemo()
}
