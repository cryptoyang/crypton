package cc.cryptopunks.crypton.mock

import cc.cryptopunks.crypton.context.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlin.coroutines.CoroutineContext

class MockState(
    val account: Address
) : CoroutineScope {

    val defaults = Defaults(account)

    val contacts = ConflatedBroadcastChannel(defaults.contacts.toSet())

    val rosterEvents = Channel<Roster.Net.Event>(Channel.BUFFERED)

    val messageEvents = Channel<Message.Net.Event>(Channel.BUFFERED)

    val apiEvents = BroadcastChannel<Api.Event>(Channel.BUFFERED)

    override val coroutineContext: CoroutineContext =
        SupervisorJob() + newSingleThreadContext(MockState::class.java.simpleName)

    operator fun invoke(block: suspend MockState.() -> Unit) {
        launch { block() }
    }

    class Defaults(
        account: Address
    ) {

        val resource = Resource(account, "mock")

        val contacts = listOf(
            "user1@cryptopunks.mock",
            "user2@cryptopunks.mock"
        ).map {
            User(Address.from(it))
        }

        val chats = contacts.map {
            Chat(
                title = it.address.toString(),
                address = it.address,
                account = account,
                resource = Resource(it.address, "mock"),
                users = listOf(it, User(account))
            )
        }

        val messages = chats.map { chat ->
            listOf(
                "message1",
                "message2"
            ).map { messageText ->
                Message(
                    id = messageText + chat.address,
                    stanzaId = messageText + chat.address,
                    chatAddress = chat.address,
                    from = chat.resource,
                    to = Resource(account, "mock"),
                    status = Message.Status.Read,
                    notifiedAt = 0,
                    readAt = 0,
                    timestamp = 0,
                    text = messageText
                )
            }
        }
    }
}

suspend operator fun <T> ConflatedBroadcastChannel<T>.invoke(f: T.() -> T?): T? =
    value.f()?.also { send(it) }
