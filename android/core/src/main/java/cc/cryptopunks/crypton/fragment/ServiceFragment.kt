package cc.cryptopunks.crypton.fragment

import android.os.Bundle
import android.view.View
import cc.cryptopunks.crypton.context.Actor
import cc.cryptopunks.crypton.context.Connectable
import cc.cryptopunks.crypton.service.ConnectableBuffer
import cc.cryptopunks.crypton.service.createBinding
import cc.cryptopunks.crypton.service.remove
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking

abstract class ServiceFragment :
    FeatureFragment(),
    Connectable {

    protected val binding: Connectable.Binding by lazy {
        runBlocking { appScope.connectableBindingsStore.createBinding() }
    }

    protected val viewProxy = ConnectableBuffer(SupervisorJob() + Dispatchers.IO)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding + viewProxy
        binding + onCreateService()
    }

    protected open fun onCreateService(): Connectable? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewProxy.service = onCreateActor(view)
    }

    @Suppress("UNCHECKED_CAST")
    protected open fun onCreateActor(view: View): Connectable? = view as? Connectable

    override fun onStart() {
        super.onStart()
        binding.send(Actor.Start)
    }

    override fun onStop() {
        super.onStop()
        binding.send(Actor.Stop)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewProxy.service = null
    }

    override fun onDestroy() {
        super.onDestroy()
        runBlocking {
            viewProxy.cancel()
            binding.cancel()
            log.d("binding canceled")
            appScope.connectableBindingsStore.remove(binding)
            log.d("binding removed")
        }
        log.d("destroyed")
    }
}
