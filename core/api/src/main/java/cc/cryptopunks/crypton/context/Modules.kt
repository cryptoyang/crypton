package cc.cryptopunks.crypton.context

import cc.cryptopunks.crypton.Connectable
import cc.cryptopunks.crypton.Context
import cc.cryptopunks.crypton.HandlerRegistry
import cc.cryptopunks.crypton.Scope
import cc.cryptopunks.crypton.util.Executors
import cc.cryptopunks.crypton.util.IOExecutor
import cc.cryptopunks.crypton.util.MainExecutor
import cc.cryptopunks.crypton.util.Store
import cc.cryptopunks.crypton.util.typedLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.newSingleThreadContext
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass

class AppModule(
    val sys: Sys,
    val repo: Repo,
    override val mainClass: KClass<*>,
    override val handlers: HandlerRegistry,
    override val createConnection: Connection.Factory,
    override val mainExecutor: MainExecutor,
    override val ioExecutor: IOExecutor,
    override val navigateChatId: Int = 0
) :
    AppScope,
    Executors,
    Sys by sys,
    Repo by repo {

    override val log = typedLog()

    override val coroutineContext: CoroutineContext = log + SupervisorJob().apply {
        invokeOnCompletion { log.d("Finish AppModule ${this@AppModule}") }
    } + Dispatchers.IO

    override val sessions = SessionScope.Store()
    override val clipboardStore = Clip.Board.Store()
    override val connectableBindingsStore = Connectable.Binding.Store()
    override val lastAccounts = Store(Account.Service.Accounts(emptyList()))
    override val rosterItems = Store(Roster.Service.Items(emptyList()))

    override fun sessionScope(): SessionScope = sessions.get().values.first()
    override fun sessionScope(address: Address): SessionScope =
        sessions[address] ?: throw Exception(
            "Cannot resolve SessionScope for $address\n" +
                "available sessions: ${sessions.get().keys.joinToString("\n")}"
        )

    override suspend fun resolve(context: Context): Pair<Scope, Any> =
        sessionScope(address(context.id)).let { scope ->
            when (val any = context.any) {
                is Context -> scope.resolve(any)
                else -> scope to any
            }
        }
}

class SessionModule(
    override val appScope: AppScope,
    val connection: Connection,
    val sessionRepo: SessionRepo,
    override val address: Address
) :
    SessionScope,
    AppScope by appScope,
    Net by connection,
    SessionRepo by sessionRepo {

    override val log = typedLog()

    override val coroutineContext: CoroutineContext = log + SupervisorJob().apply {
        invokeOnCompletion { log.d("Finish SessionModule $address ${it.hashCode()} $it") }
    } + newSingleThreadContext(address.id)

    override val presenceStore = Presence.Store()
    override val subscriptions = Store(emptySet<Address>())

    override fun chatScope(chat: Chat): ChatScope = ChatModule(this, chat)
    override suspend fun chatScope(chat: Address): ChatScope = chatScope(chatRepo.get(chat))


    override suspend fun resolve(
        context: Context
    ): Pair<Scope, Any> = when {
        context.id == address.id -> when (val any = context.any) {
            is Context -> resolve(any)
            else -> this to context.any
        }
        chatRepo.contains(address(context.id)) -> chatScope(address(context.id)).resolve(context)
        else -> appScope.resolve(context)
    }
}

class ChatModule(
    private val sessionScope: SessionScope,
    override val chat: Chat
) :
    SessionScope by sessionScope,
    ChatScope {

    override val log = typedLog()

    override val pagedMessage: Store<Chat.Service.PagedMessages?> = Store(null)

    @Suppress("IntroduceWhenSubject")
    override suspend fun resolve(
        context: Context
    ): Pair<Scope, Any> = when {
        context.id == chat.address.id -> this to context.any
        else -> sessionScope.resolve(context)
    }
}
