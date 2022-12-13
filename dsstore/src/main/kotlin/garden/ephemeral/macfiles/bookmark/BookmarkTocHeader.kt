package garden.ephemeral.macfiles.bookmark

import garden.ephemeral.macfiles.common.io.DataInput
import garden.ephemeral.macfiles.common.io.DataOutput

data class BookmarkTocHeader(
    val size: Int,
    val magic: Int,
    val tocId: Int,
    val nextTocOffset: Int,
    val entryCount: Int
) {
    fun writeTo(stream: DataOutput) {
        stream.writeIntLE(size)
        stream.writeIntLE(magic)
        stream.writeIntLE(tocId)
        stream.writeIntLE(nextTocOffset)
        stream.writeIntLE(entryCount)
    }

    companion object {
        const val SIZE = 20
        const val TOC_MAGIC = 0xFFFFFFFE.toInt()

        fun readFrom(stream: DataInput): BookmarkTocHeader {
            val tocsize = stream.readIntLE()
            val tocmagic = stream.readIntLE()
            val tocid = stream.readIntLE()
            val nexttoc = stream.readIntLE()
            val toccount = stream.readIntLE()
            return BookmarkTocHeader(tocsize, tocmagic, tocid, nexttoc, toccount)
        }
    }
}