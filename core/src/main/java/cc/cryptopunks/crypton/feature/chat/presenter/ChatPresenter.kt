package cc.cryptopunks.crypton.feature.chat.presenter

import androidx.paging.PagedList
import cc.cryptopunks.crypton.actor.Actor
import cc.cryptopunks.crypton.entity.Chat
import cc.cryptopunks.crypton.feature.chat.interactor.SendMessageInteractor
import cc.cryptopunks.crypton.feature.chat.selector.MessagePagedListSelector
import cc.cryptopunks.crypton.presenter.Presenter
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

class ChatPresenter @Inject constructor(
    private val chat: Chat,
    private val sendMessage: SendMessageInteractor,
    private val createMessagePresenter: MessagePresenter.Factory,
    private val messageFlow: MessagePagedListSelector
) : Presenter<ChatPresenter.View> {

    private val send: suspend (String) -> Unit = { sendMessage(it) }

    override suspend fun View.invoke() = coroutineScope {
        launch { sendMessageFlow.collect(send) }
        launch { messageFlow(chat, createMessagePresenter).collect(setMessages) }
    }

    interface View : Actor {
        val sendMessageFlow: Flow<String>
        val setMessages: suspend (PagedList<MessagePresenter>) -> Unit
    }
}