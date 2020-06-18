package cc.cryptopunks.crypton.interactor

import cc.cryptopunks.crypton.context.Address
import cc.cryptopunks.crypton.context.Message
import cc.cryptopunks.crypton.context.SessionScope
import kotlinx.coroutines.flow.first

internal suspend fun SessionScope.flushQueuedMessages() =
    flushQueuedMessages { true }

internal suspend fun SessionScope.flushQueuedMessages(
    address: Address
) = flushQueuedMessages { message: Message ->
    message.chatAddress == address
}

internal suspend fun SessionScope.flushQueuedMessages(
    filter: (Message) -> Boolean
) {
    messageRepo.queuedListFlow().first().let { list: List<Message> ->
        log.d("Flush pending messages $list")
        list.filter(filter).forEach { message ->
            sendMessage(message)
        }
    }
}