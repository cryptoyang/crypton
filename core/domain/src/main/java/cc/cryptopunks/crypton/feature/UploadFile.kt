package cc.cryptopunks.crypton.feature

import cc.cryptopunks.crypton.cliv2.command
import cc.cryptopunks.crypton.cliv2.config
import cc.cryptopunks.crypton.cliv2.param
import cc.cryptopunks.crypton.context.AesGcm
import cc.cryptopunks.crypton.context.Crypto
import cc.cryptopunks.crypton.context.Exec
import cc.cryptopunks.crypton.context.Message
import cc.cryptopunks.crypton.context.URI
import cc.cryptopunks.crypton.context.chat
import cc.cryptopunks.crypton.context.createEmptyMessage
import cc.cryptopunks.crypton.context.cryptoSys
import cc.cryptopunks.crypton.context.encodeString
import cc.cryptopunks.crypton.context.fileName
import cc.cryptopunks.crypton.context.fileSys
import cc.cryptopunks.crypton.context.messageRepo
import cc.cryptopunks.crypton.context.parseUriData
import cc.cryptopunks.crypton.context.uploadNet
import cc.cryptopunks.crypton.context.uriSys
import cc.cryptopunks.crypton.factory.handler
import cc.cryptopunks.crypton.feature
import cc.cryptopunks.crypton.inScope
import cc.cryptopunks.crypton.logv2.d
import cc.cryptopunks.crypton.util.rename
import cc.cryptopunks.crypton.util.useCopyTo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext

fun uploadFile() = feature(

    command(
        config("account"),
        config("chat"),
        param().copy(name = "file", description = "Path to the file for upload."),
        name = "upload file",
        description = "Upload file to server and share link in chat."
    ) { (account, chat, file) ->
        Exec.Upload(URI(file)).inScope(account, chat)
    },

    handler = handler { out, (uri): Exec.Upload ->
        val uriSys = uriSys
        val extensions = uriSys.getMimeType(uri).split("/").last().replace("*", "")
        val fileName = uri.path.parseUriData().fileName

        val secure = AesGcm.Secure()

        val encryptedFile = withContext(Dispatchers.IO) {
            fileSys.tmpDir().resolve("$fileName.$extensions").apply {
                createNewFile()
                deleteOnExit()
                cryptoSys.transform(
                    stream = uriSys.inputStream(uri),
                    secure = secure,
                    mode = Crypto.Mode.Encrypt
                ).useCopyTo(outputStream())
            }
        }

        uploadNet.upload(encryptedFile).debounce(100)
//        .scan(Message()) { message, progress ->
//            progress.run {
//                message.copy(
//                    body = Message.Text("$uploadedBytes/$totalBytes $url")
//                )
//            }
//        }
            .onEach {
                log.d { it }
                it.out()
//            messageRepo.insertOrUpdate(it)
            }
            .toList()
            .last()
            .let { result ->
                val aesGcmUrl = AesGcm.Link(
                    url = result.url!!.toString(),
                    secure = secure
                )
                encryptedFile.rename { "$it.up" }
                messageRepo.insertOrUpdate(
                    chat.createEmptyMessage().copy(
                        body = aesGcmUrl.encodeString(),
                        type = Message.Type.Url
                    )
                )
            }
    }
)
