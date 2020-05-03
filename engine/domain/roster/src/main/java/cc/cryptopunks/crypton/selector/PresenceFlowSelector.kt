package cc.cryptopunks.crypton.selector

import cc.cryptopunks.crypton.context.Address
import cc.cryptopunks.crypton.context.Presence
import cc.cryptopunks.crypton.context.UserPresence
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull

internal class PresenceFlowSelector(
    presenceChangedStore: UserPresence.Store
) : (Address) -> Flow<Presence.Status> by { address ->
    presenceChangedStore.changesFlow().mapNotNull { map: Map<Address, Presence> ->
        map[address]?.status
    }
}
