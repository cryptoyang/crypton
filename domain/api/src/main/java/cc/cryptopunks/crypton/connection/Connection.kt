package cc.cryptopunks.crypton.connection

import cc.cryptopunks.crypton.entity.Address
import cc.cryptopunks.crypton.net.Net
import cc.cryptopunks.crypton.util.BroadcastErrorScope

interface Connection : Net {

    interface Component {
        val createConnection: Factory
    }

    interface Factory : (Config) -> Connection {
        data class Config(
            val resource: String = "",
            val hostAddress: String? = null,
            val securityMode: SecurityMode = SecurityMode.ifpossible
        ) {
            enum class SecurityMode {
                required,
                ifpossible,
                disabled
            }

            companion object {
                val Empty =
                    Config()
            }
        }
    }

    data class Config(
        val scope: BroadcastErrorScope = BroadcastErrorScope(),
        val address: Address = Address.Empty,
        val password: String = ""
    ) {
        companion object {
            val Empty = Config()
        }
    }
}