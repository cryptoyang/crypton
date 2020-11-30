package cc.cryptopunks.crypton.util

import java.io.InputStream
import java.io.OutputStream

internal fun InputStream.useCopyTo(out: OutputStream) = use { i ->
    out.use { o -> i.copyTo(o) }
}
