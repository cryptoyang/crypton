package cc.cryptopunks.crypton.smack.net.chat

import cc.cryptopunks.crypton.context.Address
import cc.cryptopunks.crypton.context.Message
import cc.cryptopunks.crypton.smack.util.ext.hasOmemoExtension
import cc.cryptopunks.crypton.smack.util.ext.removeOmemoBody
import cc.cryptopunks.crypton.smack.util.ext.replaceBody
import cc.cryptopunks.crypton.smack.util.toCryptonMessage
import cc.cryptopunks.crypton.util.BroadcastErrorScope
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import org.jivesoftware.smack.chat2.Chat
import org.jivesoftware.smack.chat2.ChatManager
import org.jivesoftware.smack.chat2.IncomingChatMessageListener
import org.jivesoftware.smack.chat2.OutgoingChatMessageListener
import org.jivesoftware.smack.packet.Stanza
import org.jivesoftware.smackx.carbons.packet.CarbonExtension
import org.jivesoftware.smackx.omemo.OmemoManager
import org.jivesoftware.smackx.omemo.OmemoMessage
import org.jivesoftware.smackx.omemo.listener.OmemoMessageListener
import org.jxmpp.jid.Jid
import org.jxmpp.jid.impl.JidCreate
import org.jivesoftware.smack.packet.Message as SmackMessage

internal class MessageBroadcast(
    address: Address,
    scope: BroadcastErrorScope,
    sendMessage: SendMessage,
    private val chatManager: ChatManager,
    private val omemoManager: OmemoManager,
    private val outgoingMessageCache: OutgoingMessageCache
) : Message.Net.Broadcast {

    private val userJid = JidCreate.from(address)

    private val channel = BroadcastChannel<Message.Event>(Channel.CONFLATED)

    init {
        scope.launch {
            flowOf(
                sendMessage,
                callbacks
            )
                .flattenMerge()
                .collect(channel::send)
        }
    }

    private val callbacks
        get() = messageFlow(
            userJid = userJid,
            outgoingMessageCache = outgoingMessageCache,
            chatManager = chatManager,
            omemoManager = omemoManager
        ).mapNotNull { (smackMessage, eventType) ->

            val message = smackMessage.toCryptonMessage()

            when (smackMessage.type) {

                SmackMessage.Type.chat
                -> when (eventType) {

                    MessageType.Outgoing
                    -> Message.Event.Sending(message)

                    MessageType.Incoming,
                    MessageType.CarbonCopy
                    -> Message.Event.Received(message)
                }

                else -> null
            }
        }

    @InternalCoroutinesApi
    override suspend fun collect(collector: FlowCollector<Message.Event>) =
        channel.asFlow().collect(collector)
}

enum class MessageType {
    Incoming,
    Outgoing,
    CarbonCopy
}

private typealias MessageEvent = Pair<SmackMessage, MessageType>

private fun messageFlow(
    userJid: Jid,
    outgoingMessageCache: OutgoingMessageCache,
    chatManager: ChatManager,
    omemoManager: OmemoManager
): Flow<MessageEvent> =

    callbackFlow {

        val incomingListener = incomingListener()
        val outgoingListener = outgoingListener(userJid, outgoingMessageCache)
        val omemoListener = omemoListener()

        chatManager.addIncomingListener(incomingListener)
        chatManager.addOutgoingListener(outgoingListener)
        omemoManager.addOmemoMessageListener(omemoListener)

        awaitClose {
            chatManager.removeIncomingListener(incomingListener)
            chatManager.removeOutgoingListener(outgoingListener)
            omemoManager.removeOmemoMessageListener(omemoListener)
        }
    }


private fun SendChannel<MessageEvent>.outgoingListener(
    userJid: Jid,
    outgoingMessageCache: OutgoingMessageCache
) =
    OutgoingChatMessageListener { _, message, _ ->
        message.apply {
            from = userJid
            if (hasOmemoExtension) {
                removeOmemoBody()
                body = outgoingMessageCache[message.stanzaId]
            }
        }.takeIf {
            it.body.isNotBlank()
        }?.let {
            offer(it to MessageType.Outgoing)
        }
    }


private fun SendChannel<MessageEvent>.incomingListener() =
    IncomingChatMessageListener { _, message, _: Chat ->
        if (!message.hasOmemoExtension)
            offer(message to MessageType.Incoming)
    }


private fun SendChannel<MessageEvent>.omemoListener() = object : OmemoMessageListener {

    override fun onOmemoMessageReceived(
        stanza: Stanza,
        decryptedMessage: OmemoMessage.Received
    ) {
        stanza.let { it as? SmackMessage }
            ?.replaceBody(decryptedMessage)
            ?.let { offer(it to MessageType.Incoming) }
    }

    override fun onOmemoCarbonCopyReceived(
        direction: CarbonExtension.Direction,
        carbonCopy: SmackMessage,
        wrappingMessage: SmackMessage,
        decryptedCarbonCopy: OmemoMessage.Received
    ) {
        carbonCopy
            .replaceBody(decryptedCarbonCopy)
            ?.let { offer(it to MessageType.CarbonCopy) }
    }
}