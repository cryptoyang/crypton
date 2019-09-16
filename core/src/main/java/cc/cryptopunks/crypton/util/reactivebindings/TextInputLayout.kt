package cc.cryptopunks.crypton.util.reactivebindings

import cc.cryptopunks.crypton.util.Input
import cc.cryptopunks.kache.core.Kache
import cc.cryptopunks.kache.core.invoke
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.asFlow

suspend fun TextInputLayout.bind(property: Kache<Input>) {
    editText!!.run {
        coroutineScope {
            var current: String? = null
            launch {
                property.asFlow().collect { input ->
                    if (input.text != current) {
                        current = input.text
                        setText(input.text)
                        error = input.error.takeIf(String::isNotBlank)
                    }
                }
            }
            launch {
                textChangesPublisher().asFlow().map { it.toString() }.collect { new ->
                    if (text.toString() != current) {
                        current = new
                        property {
                            copy(text = new)
                        }
                    }
                }
            }
        }
    }
}