package cc.cryptopunks.crypton.smack

import cc.cryptopunks.crypton.entity.ApiMessage
import cc.cryptopunks.crypton.entity.Presence
import cc.cryptopunks.crypton.entity.RemoteId
import cc.cryptopunks.crypton.entity.ResourceId
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
    from = message.from.resourceId(),
    to = message.to.resourceId(),
    timestamp = delayInformation.stamp.time,
    stanzaId = message.stanzaId
)


internal fun String.resourceId() = JidCreate.from(toString()).resourceId()

internal fun String.remoteId() = JidCreate.from(toString()).remoteId()

internal fun SmackJid.resourceId() = ResourceId(
    remoteId = remoteId(),
    resource = resourceOrNull?.toString() ?: ""
)

internal fun SmackJid.remoteId() = RemoteId(
    local = localpartOrNull?.toString() ?: "",
    domain = domain.toString()
)

internal fun SmackPresence.presence() = Presence(
    status = Presence.Status.values()[type.ordinal]
)