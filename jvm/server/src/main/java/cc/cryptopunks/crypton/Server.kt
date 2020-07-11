package cc.cryptopunks.crypton

import cc.cryptopunks.crypton.backend.BackendService
import cc.cryptopunks.crypton.context.AppModule
import cc.cryptopunks.crypton.context.AppScope
import cc.cryptopunks.crypton.context.Connection
import cc.cryptopunks.crypton.context.SessionScope
import cc.cryptopunks.crypton.mock.MockRepo
import cc.cryptopunks.crypton.mock.MockSys
import cc.cryptopunks.crypton.service.startSessionService
import cc.cryptopunks.crypton.smack.SmackConnectionFactory
import cc.cryptopunks.crypton.smack.initSmack
import cc.cryptopunks.crypton.util.IOExecutor
import cc.cryptopunks.crypton.util.Log
import cc.cryptopunks.crypton.util.MainExecutor
import cc.cryptopunks.crypton.util.typedLog
import io.ktor.network.selector.ActorSelectorManager
import io.ktor.network.sockets.ServerSocket
import io.ktor.network.sockets.Socket
import io.ktor.network.sockets.aSocket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import java.io.File
import java.net.InetSocketAddress

fun main() {
    Log.init(JvmLog)
    TrustAllManager.install()
    runBlocking { startServer() }
}


private object Server

private val log = Server.typedLog()

private val createConnectionFactory = SmackConnectionFactory {
    hostAddress = "127.0.0.1"
    securityMode = Connection.Factory.Config.SecurityMode.disabled
}

private val appScope: AppScope by lazy {
    AppModule(
        sys = MockSys(),
        repo = MockRepo(),
        mainClass = Nothing::class,
        ioExecutor = IOExecutor(Dispatchers.IO.asExecutor()),
        mainExecutor = MainExecutor(Dispatchers.IO.asExecutor()),
//        createConnection = MockConnectionFactory(),
        createConnection = createConnectionFactory,
        startSessionService = SessionScope::startSessionService
    )
}

suspend fun startServer() = coroutineScope {
    initSmack(File("./omemo_store"))
    startServerSocket().let { server ->
        val service = BackendService(appScope)
        launch {
            while (true) {
                val socket = server.accept()
                log.d("Socket accepted: ${socket.remoteAddress}")
                service.tryConnectTo(socket)
            }
        }.apply {
            invokeOnCompletion {
                log.d("close server $server")
                server.close()
            }
        }
    }
}

private fun startServerSocket(): ServerSocket =
    aSocket(ActorSelectorManager(newSingleThreadContext("Server")))
        .tcp()
        .bind(InetSocketAddress("127.0.0.1", 2323))
        .apply { log.d("Started at $localAddress") }

private fun BackendService.tryConnectTo(socket: Socket) = let {
    try {
        socket.connector(log).connect().apply {
            invokeOnCompletion { socket.close() }
        }
    } catch (e: Throwable) {
        e.printStackTrace()
        socket.close()
    }
}
