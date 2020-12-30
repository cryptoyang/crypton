package cc.cryptopunks.crypton

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import cc.cryptopunks.crypton.activity.MainActivity
import cc.cryptopunks.crypton.context.ApplicationId
import cc.cryptopunks.crypton.context.Chat
import cc.cryptopunks.crypton.context.Connection
import cc.cryptopunks.crypton.context.Main
import cc.cryptopunks.crypton.context.Notification
import cc.cryptopunks.crypton.context.RootScope
import cc.cryptopunks.crypton.context.Subscribe
import cc.cryptopunks.crypton.context.account
import cc.cryptopunks.crypton.context.baseRootContext
import cc.cryptopunks.crypton.context.context
import cc.cryptopunks.crypton.debug.drawer.initAppDebug
import cc.cryptopunks.crypton.feature.androidFeatures
import cc.cryptopunks.crypton.feature.androidResolvers
import cc.cryptopunks.crypton.fragment.AndroidChatNotificationFactory
import cc.cryptopunks.crypton.navigate.currentAccount
import cc.cryptopunks.crypton.room.RoomAppRepo
import cc.cryptopunks.crypton.selector.newSessionsFlow
import cc.cryptopunks.crypton.service.cryptonFeatures
import cc.cryptopunks.crypton.service.cryptonResolvers
import cc.cryptopunks.crypton.service.initExceptionService
import cc.cryptopunks.crypton.service.start
import cc.cryptopunks.crypton.smack.SmackConnectionFactory
import cc.cryptopunks.crypton.smack.initSmack
import cc.cryptopunks.crypton.sys.AndroidSys
import cc.cryptopunks.crypton.util.ActivityLifecycleLogger
import cc.cryptopunks.crypton.util.IOExecutor
import cc.cryptopunks.crypton.util.MainExecutor
import cc.cryptopunks.crypton.util.initAndroidLog
import cc.cryptopunks.crypton.util.logger.coroutineLogLabel
import cc.cryptopunks.crypton.util.logger.log
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class App :
    Application(),
    RootScope {

    private val mainActivityClass = Main(MainActivity::class.java)
    private val features = cryptonFeatures() + androidFeatures()

    override val coroutineContext by lazy {
        cryptonContext(
            baseRootContext(),
            coroutineLogLabel(),
            ApplicationId(BuildConfig.APPLICATION_ID),
            mainActivityClass,
            MainExecutor(Dispatchers.Main.asExecutor()),
            IOExecutor(Dispatchers.IO.asExecutor()),
            RoomAppRepo(this).context(),
            AndroidSys(
                application = this,
                notificationFactories = mapOf(
                    Notification.Messages::class to AndroidChatNotificationFactory(
                        context = this,
                        mainActivityClass = mainActivityClass,
                        navGraphId = R.navigation.main
                    )
                ),
                appNameResId = R.string.app_name,
                smallIconResId = R.mipmap.ic_launcher_round
            ).context(),
            SmackConnectionFactory(setupSmackConnection).asDep<Connection.Factory>(),
            features,
            cryptonResolvers() + androidResolvers(),
            Chat.NavigationId(R.id.chatFragment),
            CoroutineExceptionHandler { coroutineContext, throwable ->
                launch { log.builder.e { this.throwable = throwable } }
            }
        )
    }

    override fun onCreate() {
        super.onCreate()
        initExceptionService()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        initAndroidLog()
        initAppDebug()
        registerActivityLifecycleCallbacks(ActivityLifecycleLogger)
        initSmack(cacheDir.resolve(OMEMO_STORE_NAME))
        launch { Subscribe.AppService.start { println(this) } } // FIXME print
        launch { newSessionsFlow().collect { currentAccount = it.account.address } }
    }

    private companion object {
        private const val OMEMO_STORE_NAME = "omemo"
    }
}
