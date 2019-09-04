package cc.cryptopunks.crypton.smack

import cc.cryptopunks.crypton.entity.ApiMessage
import cc.cryptopunks.crypton.entity.Presence
import cc.cryptopunks.crypton.entity.RemoteId
import org.jivesoftware.smack.packet.Message
import org.jivesoftware.smackx.delay.packet.DelayInformation
import org.jivesoftware.smackx.sid.element.StanzaIdElement
import org.jxmpp.jid.impl.JidCreate
import java.util.*


internal fun chatMessage(
    message: Message = Message(),
    delayInformation: DelayInformation = DelayInformation(Date())
) = ApiMessage(
    id = message.extensions.filterIsInstance<StanzaIdElement>().firstOrNull()?.id ?: "",
    text = message.body,
    from = message.from.remoteId(),
    to = message.to.remoteId(),
    timestamp = delayInformation.stamp.time,
    stanzaId = message.stanzaId
)


internal fun SmackJid.remoteId() = RemoteId(
    local = localpartOrNull?.toString() ?: "",
    domain = domain.toString(),
    resource = resourceOrNull?.toString() ?: ""
)

internal fun String.remoteId(): RemoteId = JidCreate.from(toString()).remoteId()

internal fun SmackPresence.presence() = Presence(
    status = Presence.Status.values()[type.ordinal]
)