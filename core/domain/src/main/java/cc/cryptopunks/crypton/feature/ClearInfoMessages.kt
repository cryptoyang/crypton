package cc.cryptopunks.crypton.feature

import cc.cryptopunks.crypton.context.Exec
import cc.cryptopunks.crypton.context.Message
import cc.cryptopunks.crypton.context.chat
import cc.cryptopunks.crypton.context.messageRepo
import cc.cryptopunks.crypton.factory.handler
import cc.cryptopunks.crypton.feature
import cc.cryptopunks.crypton.logv2.d

internal fun clearInfoMessages() = feature(

    handler = handler {_, _: Exec.ClearInfoMessages ->
        val chatAddress = chat.address
        messageRepo.run {
            delete(list(chatAddress, Message.Type.Info))
        }
        log.d { "Info messages deleted for $chatAddress" }
    }
)