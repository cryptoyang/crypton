package cc.cryptopunks.crypton.interactor

import cc.cryptopunks.crypton.context.Address
import cc.cryptopunks.crypton.context.AppScope
import cc.cryptopunks.crypton.context.Connectable
import cc.cryptopunks.crypton.context.Message
import cc.cryptopunks.crypton.context.Notification
import cc.cryptopunks.crypton.service.top


fun AppScope.updateChatNotification(): (List<Message>) -> Unit {
    var current = emptyList<Message>()
    return { messages ->

        current.minus(messages).asNotifications().forEach {
            notificationSys.cancel(it)
        }

        current = connectableBindingsStore.consume(messages)

        current.asNotifications().forEach {
            notificationSys.show(it)
        }
    }
}

private fun List<Message>.asNotifications() = groupBy { message: Message ->
    message.chat
}.map { (address: Address, messages: List<Message>) ->
    Notification.Messages(
        chatAddress = address,
        messages = messages
    )
}

private fun Connectable.Binding.Store.consume(messages: List<Message>) = top()
    ?.services
    ?.filterIsInstance<Message.Consumer>()
    ?.let { consumers ->
        messages.filterNot { message ->
            consumers.any { consumer ->
                consumer.canConsume(message)
            }
        }
    }
    ?: messages