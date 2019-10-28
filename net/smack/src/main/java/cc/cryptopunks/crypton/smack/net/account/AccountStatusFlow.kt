package cc.cryptopunks.crypton.smack.net.account

import cc.cryptopunks.crypton.entity.Account
import cc.cryptopunks.crypton.smack.util.ConnectionEvent
import cc.cryptopunks.crypton.smack.util.connectionEventsFlow
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.mapNotNull
import org.jivesoftware.smack.tcp.XMPPTCPConnection

internal class AccountStatusFlow(
    private val connection: XMPPTCPConnection
) : Account.Net.StatusFlow {
    @InternalCoroutinesApi
    override suspend fun collect(collector: FlowCollector<Account.Status>) = connection
        .connectionEventsFlow()
        .mapNotNull {
            when (it) {
                is ConnectionEvent.Authenticated -> Account.Status.Connected
                is ConnectionEvent.ConnectionClosed -> Account.Status.Disconnected
                else -> null
            }
        }
        .collect(collector)

}