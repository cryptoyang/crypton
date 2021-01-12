package cc.cryptopunks.crypton.feature

import cc.cryptopunks.crypton.context.Address
import cc.cryptopunks.crypton.context.RootScopeTag
import cc.cryptopunks.crypton.context.SessionScopeTag
import cc.cryptopunks.crypton.context.Subscribe
import cc.cryptopunks.crypton.context.account
import cc.cryptopunks.crypton.context.inScope
import cc.cryptopunks.crypton.context.sessions
import cc.cryptopunks.crypton.create.cryptonContext
import cc.cryptopunks.crypton.create.emission
import cc.cryptopunks.crypton.create.emitter
import cc.cryptopunks.crypton.create.handler
import cc.cryptopunks.crypton.feature
import cc.cryptopunks.crypton.logv2.d
import cc.cryptopunks.crypton.logv2.log
import cc.cryptopunks.crypton.service.start
import cc.cryptopunks.crypton.util.Log
import cc.cryptopunks.crypton.util.logger.CoroutineLog
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.withContext

internal fun startSessionService() = feature(

    emitter(RootScopeTag) {
        mutableSetOf<Address>().let { last ->
            sessions.changesFlow().flatMapConcat { current ->
                val new = (current - last)
                last.clear()
                last += current.keys
                new.map { it.value }.asFlow()
            }.distinctUntilChanged().map { scope ->
                val address = scope.account.address
                log.d { "Request start services $address" }
                Subscribe.SessionService.inScope(address)
            }.onCompletion {
                log.d { "Close newSessionsFlow" }
            }
        }
    },

    handler { _, _: Subscribe.SessionService ->
        withContext(
            cryptonContext(
                CoroutineLog.Label("SessionService"),
                CoroutineLog.Action(Subscribe.SessionService),
                CoroutineLog.Status(Log.Event.Status.Handling),
            )
        ) {
            log.d { "Invoke session services for $account" }
            emission(SessionScopeTag).start { println(this) }
        }
    }
)
