package cc.cryptopunks.crypton.notification

import android.app.Activity
import android.app.Application
import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import cc.cryptopunks.crypton.chat.R
import cc.cryptopunks.crypton.entity.Indicator
import cc.cryptopunks.crypton.entity.Message
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

internal class ShowMessageNotification @Inject constructor(
    private val context: Application,
    private val showNotification: ShowSystemNotification,
    private val mainActivityClass: Class<out Activity>
) : Message.Sys.ShowNotification, (Message) -> Unit {

    private val dateFormat: DateFormat = SimpleDateFormat("d MMM • HH:mm", Locale.getDefault())

    override fun invoke(message: Message) = message.showNotification()

    private fun Message.showNotification() = showNotification(
        notificationId,
        createNotification()
    )

    private val Message.notificationId get() = id.hashCode()

    private fun Message.createNotification(): Notification = context
        .notificationBuilder(Indicator.Notification.channelId)
        .setContentTitle(from.address)
        .setContentText(text)
        .setSubText(dateFormat.format(timestamp))
        .setSmallIcon(R.mipmap.ic_launcher_round)
        .setContentIntent(pendingIntent())
        .setAutoCancel(true)
        .build()


    private fun pendingIntent() = PendingIntent
        .getActivity(context, 0, mainActivityIntent(), 0)

    private fun mainActivityIntent() = Intent(
        context,
        mainActivityClass
    )
}