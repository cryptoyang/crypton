package cc.cryptopunks.crypton.handler

import cc.cryptopunks.crypton.context.Roster
import cc.cryptopunks.crypton.context.handle
import cc.cryptopunks.crypton.selector.RosterItemStateListFlowSelector
import cc.cryptopunks.crypton.util.Store
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

internal fun CoroutineScope.handleRosterItemsSubscription(
    rosterListFlowSelector: RosterItemStateListFlowSelector,
    lastItems: Store<Roster.Service.Items>
) = handle<Roster.Service.SubscribeItems> { output ->
    launch {
        rosterListFlowSelector()
            .map { Roster.Service.Items(it) }
            .onEach { lastItems { it } }
            .collect(output)
    }
}
