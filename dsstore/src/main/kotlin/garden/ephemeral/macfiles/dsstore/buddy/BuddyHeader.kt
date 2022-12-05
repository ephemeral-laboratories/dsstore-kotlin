package garden.ephemeral.macfiles.dsstore.buddy

import garden.ephemeral.macfiles.dsstore.types.Blob
import garden.ephemeral.macfiles.dsstore.types.FourCC
import garden.ephemeral.macfiles.dsstore.util.DataInput
import garden.ephemeral.macfiles.dsstore.util.DataOutput

/**
 * Structure at the start of the buddy allocation file.
 *
 * @property magic the magic number "Bud1".
 * @property rootBlockOffset the offset to the allocator's bookkeeping block.
 * @property rootBlockSize the size of the allocator's bookkeeping block.
 * @property rootBlockOffset2 a second copy of the offset; the Finder will refuse to read the file if this
 *           does not match the first copy. Perhaps this is a safeguard against corruption from an interrupted
 *           write transaction.
 * @property 16 bytes of unknown purpose. These might simply be the unused space at the end of the block.
 */
data class BuddyHeader(
    val magic: FourCC,
    val rootBlockOffset: Int,
    val rootBlockSize: Int,
    val rootBlockOffset2: Int,
    val unknown16: Blob,
) {
    init {
        require (magic == MAGIC) { "Magic $magic number doesn't match expected $MAGIC" }
        require(rootBlockOffset == rootBlockOffset2) {
            "Offsets $rootBlockOffset and $rootBlockOffset2 do not match"
        }
    }

    /**
     * Writes the buddy header to a stream.
     *
     * @param stream the stream to write to.
     */
    fun writeTo(stream: DataOutput) {
        stream.writeFourCC(magic)
        stream.writeInt(rootBlockOffset)
        stream.writeInt(rootBlockSize)
        stream.writeInt(rootBlockOffset2)
        stream.writeBlob(unknown16)
    }

    companion object {
        const val SIZE = 32
        val MAGIC = FourCC("Bud1")

        /**
         * Reads the buddy header from a stream.
         *
         * @param stream the stream to read from.
         * @return the read header.
         */
        fun readFrom(stream: DataInput): BuddyHeader {
            val magic = stream.readFourCC()
            val rootBlockOffset = stream.readInt()
            val rootBlockSize = stream.readInt()
            val rootBlockOffset2 = stream.readInt()
            val unknown16 = stream.readBlob(16)
            return BuddyHeader(magic, rootBlockOffset, rootBlockSize, rootBlockOffset2, unknown16)
        }
    }
}