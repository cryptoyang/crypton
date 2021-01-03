package cc.cryptopunks.crypton.feature

import cc.cryptopunks.crypton.context.RootScopeTag
import cc.cryptopunks.crypton.context.Subscribe
import cc.cryptopunks.crypton.createEmitters
import cc.cryptopunks.crypton.cryptonContext
import cc.cryptopunks.crypton.factory.handler
import cc.cryptopunks.crypton.feature
import cc.cryptopunks.crypton.interactor.loadSessions
import cc.cryptopunks.crypton.logv2.d
import cc.cryptopunks.crypton.service.start
import cc.cryptopunks.crypton.util.Log
import cc.cryptopunks.crypton.util.logger.CoroutineLog
import kotlinx.coroutines.withContext

internal fun startAppServices() = feature(

    handler = handler { _, _: Subscribe.AppService ->
        withContext(
            cryptonContext(
                CoroutineLog.Label("AppService"),
                CoroutineLog.Status(Log.Event.Status.Handling)
            )
        ) {

            log.d { "Start AppService" }

            loadSessions()

            createEmitters(RootScopeTag).start { }

            log.d { "Finish AppService" }
        }
    }
)