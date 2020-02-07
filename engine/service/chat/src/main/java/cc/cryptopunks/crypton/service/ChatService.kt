package cc.cryptopunks.crypton.service

import androidx.paging.PagedList
import cc.cryptopunks.crypton.context.*
import cc.cryptopunks.crypton.interactor.MarkMessagesAsRead
import cc.cryptopunks.crypton.interactor.SendMessageInteractor
import cc.cryptopunks.crypton.selector.MessagePagedListSelector
import cc.cryptopunks.crypton.util.ext.map
import cc.cryptopunks.crypton.util.typedLog
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class ChatService @Inject constructor(
    val chat: Chat,
    private val account: Address,
    private val sendMessage: SendMessageInteractor,
    private val markMessagesAsRead: MarkMessagesAsRead,
    private val messageFlow: MessagePagedListSelector,
    private val clipboardRepo: Clip.Board.Repo,
    private val copyToClipboard: Clip.Board.Sys.SetClip
) :
    Service.Connectable,
    Message.Consumer {

    object CopyMessageText

    data class MessageOption(val id: Any, val message: Message)
    data class SendMessage(val text: String)
    data class MessagesRead(val messages: List<Message>)
    data class Messages(val account: Address, val list: PagedList<Message>)
    data class MessageText(val text: CharSequence?)

    override val coroutineContext = SupervisorJob() + Dispatchers.IO

    private val log = typedLog()

    private var status: Any = Service.Actor.Stop

    override fun Service.Connector.connect(): Job = launch {
        launch {
            input.collect {
                log.d("in: $it")
                when (it) {
                    is Service.Actor.Stop -> {
                        status = it
                    }
                    is Service.Actor.Start -> {
                        status = it
                        clipboardRepo.pop()?.run {
                            output(MessageText(data))
                        }
                    }
                    is Service.Actor.Connected -> {
                        clipboardRepo.pop()?.run {
                            output(MessageText(data))
                        }
                    }
                    is MessagesRead -> {
                        markMessagesAsRead(it.messages)
                    }
                    is SendMessage -> if (it.text.isNotBlank()) {
                        sendMessage(it.text)
                    }
                    is MessageOption -> when (it.id) {
                        is CopyMessageText ->
                            copyToClipboard(it.message.text)
                    }
                }
            }
        }
        launch {
            messageFlow(chat)
                .onEach { log.d("received ${it.size} messages") }
                .map { Messages(account, it) }
                .collect(output)
        }.invokeOnCompletion {
            log.d("Message flow completed")
            it?.let(log::e)
        }
    }

    override fun canConsume(message: Message): Boolean =
        message.chatAddress == chat.address && status == Service.Actor.Start


    interface Core {
        val chatService: ChatService
    }
}