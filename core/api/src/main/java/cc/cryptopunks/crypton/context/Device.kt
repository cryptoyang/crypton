package cc.cryptopunks.crypton.context

import cc.cryptopunks.crypton.dep
import kotlinx.coroutines.flow.Flow

val RootScope.deviceSys: Device.Sys by dep()
val SessionScope.deviceRepo: Device.Repo by dep()
val SessionScope.deviceNet: Device.Net by dep()

data class Device(
    val id: Int = -1,
    val address: Address = Address.Empty
) {

    data class Fingerprint(
        val value: CharSequence,
        val device: Device,
        val state: TrustState = TrustState.Unset
    )

    enum class TrustState {
        Undecided,
        Untrusted,
        Trusted,
        Unset
    }


    interface Repo {
        suspend fun set(fingerprint: Fingerprint)
        suspend fun get(value: CharSequence) : Fingerprint?
        suspend fun clear()
    }

    interface Net {
        fun trust(fingerprint: Fingerprint)
        fun distrust(fingerprint: Fingerprint)
        fun setDeviceFingerprintRepo(repo: Repo)
        fun listActiveFingerprints(address: Address) : List<Fingerprint>
        fun fingerprintStateChangedFlow() : Flow<Fingerprint>
        fun purgeDeviceList()
    }

    interface Sys {
        fun deviceId(): String
    }
}
