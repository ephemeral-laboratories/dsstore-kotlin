package garden.ephemeral.macfiles.bookmark

/**
 * A key for looking up an entry in the table of contents.
 */
sealed class TocKey {
    data class OfInt(val value: Int) : TocKey()
    data class OfString(val value: String) : TocKey()
}
