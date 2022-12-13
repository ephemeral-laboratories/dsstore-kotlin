package garden.ephemeral.macfiles.bookmark

import garden.ephemeral.macfiles.common.io.DataInput
import garden.ephemeral.macfiles.common.io.DataOutput
import garden.ephemeral.macfiles.common.types.FourCC

/**
 * Structure for the bookmark file header.
 */
data class BookmarkHeader(
    val magic: FourCC,
    val size: Int,
    val dummy: Int,
    val headerSize: Int
) {
    init {
        require(magic in setOf(MAGIC1, MAGIC2)) { "Not a bookmark file (bad magic) $magic" }
        require(headerSize >= 16) { "Not a bookmark file (header size too short)" }
    }

    fun writeTo(stream: DataOutput) {
        stream.writeFourCC(magic)
        stream.writeIntLE(size)
        stream.writeIntLE(dummy)
        stream.writeIntLE(headerSize)
    }

    companion object {
        const val SIZE = 16

        val MAGIC1 = FourCC("book")
        val MAGIC2 = FourCC("alis")

        fun readFrom(stream: DataInput): BookmarkHeader {
            val magic = stream.readFourCC()
            val size = stream.readIntLE()
            val dummy = stream.readIntLE()
            val headerSize = stream.readIntLE()
            return BookmarkHeader(magic, size, dummy, headerSize)
        }
    }
}
