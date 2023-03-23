package garden.ephemeral.macfiles.native

import com.sun.jna.Platform
import garden.ephemeral.macfiles.native.internal.SystemB
import java.io.File
import java.text.Normalizer

internal fun getVolumePath(file: File): File {
    // Find the filesystem
    val st = SystemB.Statfs()
    SystemB.INSTANCE.statfs(file.absolutePath.toByteArray(), st)
    // File and folder names in HFS+ are normalized to a form similar to NFD.
    // Must be normalized (NFD->NFC) before use to avoid unicode string comparison issues.
    return File(Normalizer.normalize(st.f_mntonname.decodeToString().trim('\u0000'), Normalizer.Form.NFC))
}

internal fun requireMacOS() {
    if (!Platform.isMac()) {
        throw UnsupportedOperationException("Not implemented on this platform (requires native macOS support)")
    }
}
