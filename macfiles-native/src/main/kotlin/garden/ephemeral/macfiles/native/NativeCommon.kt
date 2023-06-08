package garden.ephemeral.macfiles.native

import com.sun.jna.*
import garden.ephemeral.macfiles.native.internal.*
import java.io.*
import java.text.*

internal fun requireMacOS() {
    if (!Platform.isMac()) {
        throw UnsupportedOperationException("Not implemented on this platform (requires native macOS support)")
    }
}

@Throws(NoSuchFileException::class)
internal fun checkFileExists(file: File) {
    if (!file.exists()) {
        throw NoSuchFileException(file)
    }
}
