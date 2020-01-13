package cc.cryptopunks.crypton.fragment

import android.os.Bundle
import android.view.View
import cc.cryptopunks.crypton.context.Service
import cc.cryptopunks.crypton.service.ServiceBindingManager
import cc.cryptopunks.crypton.util.ext.resolve
import kotlinx.coroutines.cancel

abstract class ServiceFragment :
    FeatureFragment() {

    private val serviceManager by lazy {
        appCore.resolve<ServiceBindingManager.Core>().serviceBindingManager
    }

    val binding by lazy {
        serviceManager.createBinding()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding + onCreatePresenter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.slot1 = onCreateActor(view)
    }

    open fun onCreatePresenter(): Service? = null

    @Suppress("UNCHECKED_CAST")
    open fun onCreateActor(view: View): Service? = view as? Service

    override fun onDestroyView() {
        super.onDestroyView()
        binding.apply {
            slot1?.cancel()
            slot1 = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.cancel()
        serviceManager.remove(binding)
    }
}