package garden.ephemeral.macfiles.native

import com.sun.jna.*
import garden.ephemeral.macfiles.native.internal.*
import java.io.*
import java.text.*

internal fun getVolumePath(file: File): File {
    // Find the filesystem
    val st = SystemB.Statfs()
    SystemB.INSTANCE.statfs64(file.absolutePath.toByteArray(), st)
    // File and folder names in HFS+ are normalized to a form similar to NFD.
    // Must be normalized (NFD->NFC) before use to avoid unicode string comparison issues.
    return File(Normalizer.normalize(st.f_mntonname.decodeToString().trim('\u0000'), Normalizer.Form.NFC))
}

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
