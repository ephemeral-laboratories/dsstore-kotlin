package garden.ephemeral.macfiles.bookmark

import garden.ephemeral.macfiles.common.io.DataInput
import garden.ephemeral.macfiles.common.io.DataOutput

/**
 * Structure for a single entry in the TOC.
 *
 * @param encodedKey if the high bit (0x80000000) is set, the low bits are an offset to a string key.
 *        If the high bit is not set, the vlaue itself is an int key.
 * @param valueOffset the offset to the entry value.
 * @property dummy some unknown value.
 */
data class BookmarkTocEntry(
    val encodedKey: Int,
    val valueOffset: Int,
    val dummy: Int
) {
    fun writeTo(stream: DataOutput) {
        stream.writeIntLE(encodedKey)
        stream.writeIntLE(valueOffset)
        stream.writeIntLE(dummy)
    }

    companion object {
        const val SIZE = 12

        fun readFrom(stream: DataInput): BookmarkTocEntry {
            val eid = stream.readIntLE()
            val eoffset = stream.readIntLE()
            val edummy = stream.readIntLE()
            return BookmarkTocEntry(eid, eoffset, edummy)
        }
    }
}