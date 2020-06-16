package cc.cryptopunks.crypton.backend.internal

import cc.cryptopunks.crypton.context.AppScope
import cc.cryptopunks.crypton.context.Connectable
import cc.cryptopunks.crypton.context.Route
import cc.cryptopunks.crypton.context.Route.AccountList
import cc.cryptopunks.crypton.context.Route.AccountManagement
import cc.cryptopunks.crypton.context.Route.Chat
import cc.cryptopunks.crypton.context.Route.CreateChat
import cc.cryptopunks.crypton.context.Route.Dashboard
import cc.cryptopunks.crypton.context.Route.Login
import cc.cryptopunks.crypton.context.Route.Register
import cc.cryptopunks.crypton.context.Route.Roster
import cc.cryptopunks.crypton.context.Route.SetAccount
import cc.cryptopunks.crypton.module.AccountDomainModule
import cc.cryptopunks.crypton.service.ChatService
import cc.cryptopunks.crypton.module.CommonDomainModule
import cc.cryptopunks.crypton.service.CreateChatService
import cc.cryptopunks.crypton.service.RosterService
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

internal class ServiceFactory(
    private val appScope: AppScope
): (Route) -> Connectable? {
    override fun invoke(route: Route): Connectable? = when (route) {

        SetAccount -> CommonDomainModule(
            appScope = appScope
        ).routerService

        Login -> AccountDomainModule(
            appScope = appScope
        ).createAccountService

        Register -> AccountDomainModule(
            appScope = appScope
        ).createAccountService

        Dashboard -> null

        Roster -> RosterService(appScope)

        AccountList -> AccountDomainModule(
            appScope = appScope
        ).accountListService

        AccountManagement -> null

        is CreateChat -> CreateChatService(
            sessionScope = appScope.sessionScope(
                address = CreateChat(route.data).accountAddress
            )
        )

        is Chat -> ChatService(
            chatScope = runBlocking {
                delay(100)
                appScope.sessionScope(
                    address = Chat(route.data).accountAddress
                ).run {
                    chatScope(chatRepo.get(Chat(route.data).address))
                }
            }
        )

        else -> null
    }
}