package garden.ephemeral.macfiles.bookmark.types

sealed class URL {
    data class Absolute(val value: String) : URL()
    data class Relative(val base: URL, val relative: String) : URL()
}
