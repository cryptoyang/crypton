package cc.cryptopunks.crypton.sys

import cc.cryptopunks.crypton.context.Route

@Deprecated("Use findNavController")
internal class RouteSys : Route.Sys {

    override fun navigate(route: Route) {
        throw NotImplementedError()
    }

    override suspend fun bind(navigator: Any) {
        throw NotImplementedError()
    }
}
