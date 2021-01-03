package cc.cryptopunks.crypton

internal class EmbeddedConfig(
    map: Map<String, Any?> = emptyMap(),
) : MutableMap<String, Any?> by map.toMutableMap() {
    var home: String by this
    var omemoStore: String by this
    var profile: String by this
    var socketAddress: String by this
    var socketPort: Int by this
    var hostAddress: String? by this
    var securityMode: String by this
    var inMemory: String by this
}

internal fun EmbeddedConfig.default() = apply {
    home = "~/.crypton".replaceFirst("~", System.getProperty("user.home"))
    profile = "default"
    omemoStore = "omemo_store"
    socketPort = 2323
    socketAddress = "127.0.0.1"
    hostAddress = null
    securityMode = "ifpossible"
    inMemory = "false"
}

internal fun EmbeddedConfig.local() = apply {
    hostAddress = "127.0.0.1"
    securityMode = "disabled"
    profile = "local"
}