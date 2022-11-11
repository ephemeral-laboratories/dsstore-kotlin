package buddy

/**
 * Packed block offset and size, used by [RootBlockData].
 */
class BlockAddress(val value: Int) {
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
    }
}
