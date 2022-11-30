package buddy

/**
 * Packed block offset and size, used by [RootBlockData].
 */
class BlockAddress(val value: Int) {
    /**
     * Alternative constructor to provide the value in parts.
     * Avoids doing bit maths when creating an address, and the values get checked in the process.
     *
     * The block offset must be a multiple of 32.
     * The block size log 2 must be in the range 0x5..0x1f.
     *
     * @param blockOffset the block offset.
     * @param blockSizeLog2 the block size, log 2.
     */
    constructor(blockOffset: Int, blockSizeLog2: Int) : this(partsToValue(blockOffset, blockSizeLog2))

    /**
     * The least-significant 5 bits of the number indicate the block's size, as a power of 2 (from 2^5 to 2^31).
     */
    val blockSize
        get() = 1 shl (value and SIZE_BITS)

    /**
     * If the blockSize bits are masked off, the result is the starting offset of the block (keeping in mind the
     * 4-byte fudge factor). Since the lower 5 bits are unusable to store an offset, blocks must be allocated on
     * 32-byte boundaries, and as a side effect the minimum block size is 32 bytes (in which case the least
     * significant 5 bits are equal to 0x05).
     */
    val blockOffset
        get() = value and OFFSET_BITS

    companion object {
        private const val SIZE_BITS = 0x1f
        private const val OFFSET_BITS = SIZE_BITS.inv()

        private fun partsToValue(blockOffset: Int, blockSizeLog2: Int): Int {
            require(blockOffset and OFFSET_BITS.inv() == 0) {
                "Invalid block offset (bits out of range): $blockOffset"
            }
            require(blockSizeLog2 and SIZE_BITS.inv() == 0) {
                "Invalid size (log 2) (bits out of range): $blockSizeLog2"
            }
            require(blockSizeLog2 >= 5) {
                "Invalid size (log 2) (minimum size is 32 bytes, i.e. 0x5): $blockSizeLog2"
            }

            return blockOffset or blockSizeLog2
        }
    }
}
