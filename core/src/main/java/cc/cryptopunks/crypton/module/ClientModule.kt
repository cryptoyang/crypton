package cc.cryptopunks.crypton.module

import cc.cryptopunks.crypton.api.Client
import cc.cryptopunks.crypton.api.MapException
import cc.cryptopunks.crypton.component.ClientComponent

class ClientModule(
    createClient: Client.Factory,
    override val mapException: MapException,
    override val currentClient: Client.Current = Client.Current(),
    override val clientRepo: Client.Repo = Client.Repo(
        createClient = createClient,
        clientCache = Client.Cache()
    )
) : ClientComponent