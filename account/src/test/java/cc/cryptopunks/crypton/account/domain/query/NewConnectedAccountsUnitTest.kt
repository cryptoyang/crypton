package cc.cryptopunks.crypton.account.domain.query

import cc.cryptopunks.crypton.core.entity.Account
import cc.cryptopunks.crypton.core.entity.Account.Status.*
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test

class NewConnectedAccountsUnitTest {

    @RelaxedMockK
    lateinit var dao: Account.Dao
    private lateinit var newConnectedAccounts: NewConnectedAccounts

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        newConnectedAccounts = NewConnectedAccounts(dao)
    }

    @Test
    operator fun invoke() {
        // given
        val subject = PublishSubject.create<List<Account>>()
        every { dao.observeList() } returns subject

        // when
        val addedAccounts = newConnectedAccounts().test()

        subject.onNext(
            listOf(
                Account(id = 1, status = Connected),
                Account(id = 2)
            )
        )
        subject.onNext(
            listOf(
                Account(id = 1, status = Connected),
                Account(id = 2, status = Connected)
            )
        )
        subject.onNext(
            listOf(
                Account(id = 1)
            )
        )

        // then
        addedAccounts
            .assertNoErrors()
            .assertValues(1, 2)
    }
}