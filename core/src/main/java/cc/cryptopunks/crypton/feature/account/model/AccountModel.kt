package cc.cryptopunks.crypton.feature.account.model

import cc.cryptopunks.crypton.api.Client
import cc.cryptopunks.crypton.entity.Account
import cc.cryptopunks.crypton.entity.Address
import cc.cryptopunks.crypton.util.ext.reduce
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject

data class AccountModel @Inject constructor(
    private val accountRepo: Account.Repo,
    private val clientRepo: Client.Repo
) :
    AtomicReference<Account>(Account.Empty) {

    private val client: Client get() = clientRepo[get()]

    val isInitialized: Boolean get() = get() in clientRepo

    fun copy(account: Account): AccountModel = copy().apply { set(account) }

    fun setStatus(status: Account.Status): AccountModel = reduce { copy(status = status) }

    fun load(id: Address): Account = reduce { accountRepo.get(id) }.get()

    fun register(): Unit = client.create()

    fun login(): Unit = client.login()

    fun disconnect(): Unit = client.disconnect()

    fun insert(): Account = accountRepo.insert(get()).also { set(it) }

    fun update(): Unit = accountRepo.update(get())

    fun unregister() {
        client.remove()
        delete()
    }

    fun delete() {
        clear()
        accountRepo.delete(get())
    }

    fun clear() {
        clientRepo - get()
    }

    inline fun <R> run(block: AccountModel.() -> R): R =
        try {
            block()
        } catch (throwable: Throwable) {
            throw get().exception(throwable)
        }
}