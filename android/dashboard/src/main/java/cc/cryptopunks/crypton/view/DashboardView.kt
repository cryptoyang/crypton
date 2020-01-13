package cc.cryptopunks.crypton.view

import android.view.View
import cc.cryptopunks.crypton.context.OptionItem
import cc.cryptopunks.crypton.context.Service
import cc.cryptopunks.crypton.service.DashboardService
import cc.cryptopunks.crypton.util.bindings.clicks
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.dashboard.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flattenMerge
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class DashboardView(
    private val optionItems: OptionItem.Output,
    override val containerView: View
) :
    Service,
    LayoutContainer {

    override val coroutineContext = SupervisorJob() + Dispatchers.Main

    private val createChat get() = createConversationButton.clicks()

    override fun Service.Connector.connect(): Job = launch {
        flowOf(
            optionItems.map { DashboardService.ManageAccounts },
            createChat.map { DashboardService.CreateChat }
        )
            .flattenMerge()
            .collect(output)
    }
}