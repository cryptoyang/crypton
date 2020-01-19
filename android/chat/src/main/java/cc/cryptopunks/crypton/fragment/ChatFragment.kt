package cc.cryptopunks.crypton.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import cc.cryptopunks.crypton.adapter.MessageAdapter
import cc.cryptopunks.crypton.context.Address
import cc.cryptopunks.crypton.context.Route
import cc.cryptopunks.crypton.service.ChatService
import cc.cryptopunks.crypton.util.ext.resolve
import cc.cryptopunks.crypton.util.toMap
import cc.cryptopunks.crypton.view.ChatView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class ChatFragment : ServiceFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding + viewProxy
        binding + MessageAdapter()
        binding + runBlocking {
            val route = Route.Chat(arguments.toMap())
            val address = Address.from(route.chatAddress)
            val chat = withContext(Dispatchers.IO) {
                featureCore.chatRepo.get(address)
            }
            setTitle(chat.address)
            featureCore
                .sessionFeature()
                .chatFeature(chat)
                .resolve<ChatService.Core>()
                .chatService
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = ChatView(context!!)
}