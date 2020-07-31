package cc.cryptopunks.crypton

import cc.cryptopunks.crypton.backend.BackendService
import cc.cryptopunks.crypton.context.RootModule
import cc.cryptopunks.crypton.context.RootScope
import cc.cryptopunks.crypton.context.Connection
import cc.cryptopunks.crypton.mock.MockRepo
import cc.cryptopunks.crypton.mock.MockSys
import cc.cryptopunks.crypton.net.connect
import cc.cryptopunks.crypton.net.startServerSocket
import cc.cryptopunks.crypton.service.cryptonHandlers
import cc.cryptopunks.crypton.smack.SmackConnectionFactory
import cc.cryptopunks.crypton.smack.initSmack
import cc.cryptopunks.crypton.util.IOExecutor
import cc.cryptopunks.crypton.util.Log
import cc.cryptopunks.crypton.util.MainExecutor
import cc.cryptopunks.crypton.util.logger.CoroutineLog
import cc.cryptopunks.crypton.util.logger.typedLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File
import java.net.InetSocketAddress

fun main() {
    runBlocking {
        initJvmLog()
        TrustAllManager.install()
        startCryptonServer()
    }
}

suspend fun startCryptonServer() = withContext(
    CoroutineLog.Label("CryptonServer")
) {
    initSmack(File("./omemo_store"))
    startServerSocket(address).connect(BackendService(rootScope))
}

private val address = InetSocketAddress("127.0.0.1", 2323)

private val log = Server.typedLog()

private object Server

private val rootScope: RootScope
    get() = RootModule(
        sys = MockSys(),
        repo = MockRepo(),
        mainClass = Nothing::class,
        ioExecutor = IOExecutor(Dispatchers.IO.asExecutor()),
        mainExecutor = MainExecutor(Dispatchers.IO.asExecutor()),
        createConnection = createConnectionFactory,
        handlers = cryptonHandlers()
    )

private val createConnectionFactory = SmackConnectionFactory {
    hostAddress = "127.0.0.1"
    securityMode = Connection.Factory.Config.SecurityMode.disabled
}
