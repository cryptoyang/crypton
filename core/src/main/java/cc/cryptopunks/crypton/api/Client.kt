package cc.cryptopunks.crypton.api

import cc.cryptopunks.crypton.entity.*
import cc.cryptopunks.crypton.util.CacheFlow
import cc.cryptopunks.crypton.util.createDummyClass
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filterNotNull

interface Client: Api {

    val isConnected: IsConnected
    val connect: Connect
    val disconnect: Disconnect

    interface Connect: () -> Unit
    interface Disconnect: () -> Unit
    interface IsConnected: () -> Boolean

    interface Factory : (Config) -> Client {
        data class Config(
            val resource: String = "",
            val hostAddress: String = "",
            val securityMode: SecurityMode = SecurityMode.required
        ) {
            enum class SecurityMode {
                required,
                ifpossible,
                disabled
            }
            companion object {
                val Empty = Config()
            }
        }
    }

    data class Config(
        val address: Address = Address.Empty,
        val password: String = ""
    ) {
        companion object {
            val Empty = Config()
        }
    }

    class Current: CacheFlow<Client> by CacheFlow(Empty)

    class Cache internal constructor(
        private val map: MutableMap<String, Client> = mutableMapOf()
    ) :
        MutableMap<String, Client> by map,
        Flow<Client> {

        private val channel = BroadcastChannel<Client?>(Channel.CONFLATED)

        @InternalCoroutinesApi
        override suspend fun collect(collector: FlowCollector<Client>) {
            map.values.asFlow().collect(collector)
            channel.asFlow().filterNotNull().collect(collector)
        }

        override fun remove(key: String) = map
            .remove(key)
            ?.apply { send(Empty(address = address)) }

        override fun put(key: String, value: Client): Client? = map
            .put(key, value)
            .apply { send(value) }

        private fun send(client: Client) = GlobalScope.launch {
            channel.send(client)
            channel.send(null)
        }
    }

    class Manager(
        private val createClient: Factory,
        private val clientCache: Cache
    ): Flow<Client> by clientCache {

        operator fun get(account: Account): Client = synchronized(this) {
            account.run {
                clientCache[address.id] ?: createClient(
                    Config(
                        address = address,
                        password = password
                    )
                )   .apply { connect() }
                    .also { clientCache[address.id] = it }
            }
        }

        operator fun contains(account: Account): Boolean = synchronized(this) {
            account.address.id in clientCache
        }


        operator fun minus(account: Account) = synchronized(this) {
            clientCache -= account.address.id
        }
    }

    interface Component {
        val mapException: MapException
        val currentClient: Current
        val clientManager: Manager
    }

    class Exception(
        message: String? = null,
        cause: Throwable? = null
    ) : kotlin.Exception(message, cause) {

        interface Output : Flow<Exception>
    }

    class Empty(override val address: Address) : Client by Empty

    companion object {
        val Empty: Client = createDummyClass()
    }
}

typealias MapException = (Throwable) -> Throwable

val Client.isEmpty get() = this is Client.Empty