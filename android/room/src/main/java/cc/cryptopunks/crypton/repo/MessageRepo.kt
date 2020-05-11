package cc.cryptopunks.crypton.repo

import androidx.paging.DataSource
import cc.cryptopunks.crypton.context.Address
import cc.cryptopunks.crypton.context.Message
import cc.cryptopunks.crypton.entity.MessageData
import cc.cryptopunks.crypton.entity.message
import cc.cryptopunks.crypton.entity.messageData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

internal class MessageRepo(
    private val dao: MessageData.Dao,
    override val coroutineContext: CoroutineContext
) : Message.Repo,
    CoroutineScope {

    private var latest: Message = Message.Empty
        set(value) {
            if (field.timestamp < value.timestamp)
                field = value
        }

    private val unreadMessagesChannel = BroadcastChannel<List<Message>>(Channel.CONFLATED)

    init {
        launch {
            notifyUnread()
        }
    }

    override suspend fun insertOrUpdate(message: Message) {
        latest = message
        dao.insertOrUpdate(message.messageData())
    }

    override suspend fun insertOrUpdate(messages: List<Message>) {
        dao.insertOrUpdate(messages.map {
            latest = it
            it.messageData()
        })
    }

    override suspend fun notifyUnread() {
        val unread = listUnread()
        unreadMessagesChannel.send(unread)
    }

    override suspend fun get(id: String): Message? =
        dao.get(id)?.message()

    override suspend fun delete(message: Message) =
        dao.delete(message.id)

    override suspend fun latest(): Message? =
        if (latest != Message.Empty)
            latest else
            dao.latest()
                ?.message()
                ?.also { latest = it }

    override suspend fun listUnread(): List<Message> =
        dao.listUnread().map { it.message() }

    override fun flowLatest(chatAddress: Address): Flow<Message> =
        dao.flowLatest(chatAddress.id).filterNotNull().map { it.message() }

    override fun dataSourceFactory(chatAddress: Address): DataSource.Factory<Int, Message> =
        dao.dataSourceFactory(chatAddress.id).map { it.message() }

    override fun unreadListFlow(): Flow<List<Message>> =
        unreadMessagesChannel.asFlow()


    override fun unreadCountFlow(chatAddress: Address): Flow<Int> =
        unreadListFlow().map { list ->
            list.filter { message ->
                message.chatAddress == chatAddress
            }.size
        }
}
