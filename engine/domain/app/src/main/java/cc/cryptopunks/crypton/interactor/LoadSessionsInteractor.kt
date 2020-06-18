package cc.cryptopunks.crypton.interactor

import cc.cryptopunks.crypton.context.AppScope
import cc.cryptopunks.crypton.factory.createSession

internal suspend fun AppScope.loadSessions() {
    sessionStore.reduce {
        plus(
            accountRepo.addressList()
                .minus(keys)
                .map { createSession(it) }
                .map { it.address to it }
        )
    }?.also {
        log.d("Load sessions ${it.keys}")
    }
}
