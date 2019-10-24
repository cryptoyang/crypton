package cc.cryptopunks.crypton.interactor

import cc.cryptopunks.crypton.entity.Account
import cc.cryptopunks.crypton.entity.Account.Status.Disconnected
import cc.cryptopunks.crypton.manager.AccountManager
import cc.cryptopunks.crypton.service.Service
import kotlinx.coroutines.Job
import javax.inject.Inject

class RegisterAccountInteractor @Inject constructor(
    scope: Service.Scope,
    manager: AccountManager,
    login: LoginAccountInteractor,
    deleteAccount: DeleteAccountInteractor
) : (Account) -> Job by { account ->
    scope launch {
        manager.copy().run(
            onAccountException = deleteAccount
        ) {
            set(account)
            setStatus(Disconnected)
            connect()
            register()
            insert()
            login.suspend(get())
        }
    }
}