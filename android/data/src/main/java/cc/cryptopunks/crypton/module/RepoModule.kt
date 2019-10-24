package cc.cryptopunks.crypton.module

import android.content.Context
import androidx.room.Room
import cc.cryptopunks.crypton.repo.Repo
import cc.cryptopunks.crypton.data.Database
import cc.cryptopunks.crypton.entity.Account
import cc.cryptopunks.crypton.entity.Chat
import cc.cryptopunks.crypton.entity.Message
import cc.cryptopunks.crypton.entity.User
import cc.cryptopunks.crypton.repo.AccountRepo
import cc.cryptopunks.crypton.repo.ChatRepo
import cc.cryptopunks.crypton.repo.MessageRepo
import cc.cryptopunks.crypton.repo.UserRepo

class RepoModule(
    context: Context
) : Repo {

    private val database: Database = Room
        .databaseBuilder(context, Database::class.java, "crypton.db")
//        .inMemoryDatabaseBuilder(context, Database::class.java)
        .build()

    override val accountRepo: Account.Repo = AccountRepo(
        dao = database.accountDao
    )

    override val chatRepo: Chat.Repo = ChatRepo(
        chatDao = database.chatDao,
        chatUserDao = database.chatUserDao,
        userDao = database.userDao
    )

    override val messageRepo: Message.Repo = MessageRepo(
        dao = database.messageDao
    )

    override val userRepo: User.Repo = UserRepo(
        dao = database.userDao
    )
}