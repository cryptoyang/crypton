package cc.cryptopunks.crypton.service

import cc.cryptopunks.crypton.entity.Address
import cc.cryptopunks.crypton.entity.Message
import cc.cryptopunks.crypton.entity.Session
import cc.cryptopunks.crypton.entity.canConsume
import cc.cryptopunks.crypton.presentation.PresentationManager
import cc.cryptopunks.crypton.util.typedLog
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNot
import javax.inject.Inject

class MessageNotificationService @Inject constructor(
    private val scope: Session.Scope,
    private val address: Address,
    private val presentationManager: PresentationManager,
    private val showNotification: Message.Sys.ShowNotification,
    private val messageBroadcast: Message.Net.Broadcast
) : () -> Job {

    private val log = typedLog()

    override fun invoke() = scope.launch {
        log.d("start")
        messageBroadcast
            .filterNot { it.from.address == address }
            .filterNot { canConsumeMessage(it) }
            .collect { showNotification(it) }
        log.d("stop")
    }

    private fun canConsumeMessage(
        message: Message
    ): Boolean = presentationManager
        .stack()
        .filter { it.isVisible }
        .any { it.presenter.canConsume(message) }
}