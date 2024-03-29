package garden.ephemeral.macfiles.dsstore.buddy

import garden.ephemeral.macfiles.common.io.Block
import garden.ephemeral.macfiles.common.types.Blob
import garden.ephemeral.macfiles.dsstore.util.FileChannelIO
import garden.ephemeral.macfiles.dsstore.util.FileMode
import java.io.Closeable
import java.nio.file.Path

/**
 * Represents the entire buddy file.
 */
class BuddyFile(private val stream: FileChannelIO) : Closeable {
    private var header: BuddyHeader
    private var rootBlockData: RootBlockData
    private var dirty = false

    init {
        val headersBlock = stream.readBlock(0, 36)
        val magic = headersBlock.readInt()
        require(magic == FILE_MAGIC) { "File magic $magic not expected $FILE_MAGIC" }

        header = BuddyHeader.readFrom(headersBlock)

        val rootBlock = stream.readBlock(header.rootBlockOffset + 4L, header.rootBlockSize)
        rootBlockData = RootBlockData.readFrom(rootBlock)
    }

    /**
     * Tests whether the TOC contains an entry.
     *
     * @param key a TOC string, generally a short ASCII value.
     */
    fun hasTocEntry(key: String) = rootBlockData.tocEntries.containsKey(key)

    /**
     * Gets an entry from the TOC.
     *
     * @param key a TOC string, generally a short ASCII value.
     * @return the block number for that entry.
     * @throws IllegalArgumentException if the entry does not exist.
     */
    fun getTocEntry(key: String): Int {
        return rootBlockData.tocEntries[key] ?: throw IllegalArgumentException("Key does not exist: $key")
    }

    /**
     * Allocates a block for a new TOC entry.
     *
     * @param key a TOC string, generally a short ASCII value.
     * @param bytes the minimum size of the block, in bytes.
     * @return the newly-allocated block number.
     */
    fun allocateTocEntry(key: String, bytes: Int): Int {
        val newTocEntries = rootBlockData.tocEntries.toMutableMap()
        val newBlockNumber = allocateBlock(bytes)

        if (newTocEntries.putIfAbsent(key, newBlockNumber) != null) {
            throw IllegalStateException("TOC entry was already present!")
        }

        updateRootBlockData { r -> r.copy(tocEntries = newTocEntries) }

        return newBlockNumber
    }

    /**
     * Convenience method to write a block into a newly-allocated block
     * of an appropriate size.
     *
     * @param block the block to write.
     * @return the allocated block number.
     */
    fun allocateAndWriteBlock(block: Block): Int {
        val newBlockNumber = allocateBlock(block.size)
        writeBlock(newBlockNumber, block)
        return newBlockNumber
    }

    /**
     * Allocates a block.
     *
     * @param bytes the size of the block.
     * @return the allocated block number.
     */
    fun allocateBlock(bytes: Int): Int {
        val newBlockAddresses = rootBlockData.blockAddresses.toMutableList()

        // Finding first unused block number
        var block = newBlockAddresses.indexOf(null)
        if (block < 0) {
            block = newBlockAddresses.size
            newBlockAddresses.add(null)
        }

        val minimumSizeLog2 = BlockAddress.calculateMinimumSizeLog2(bytes)

        newBlockAddresses[block] = allocInner(minimumSizeLog2)

        updateRootBlockData { r -> r.copy(blockAddresses = newBlockAddresses) }

        return block
    }

    private fun allocInner(requestedSizeLog2: Int): BlockAddress {
        val newFreeLists = rootBlockData.freeLists.toMutableList()

        // Starting at the requested size, look for the first free list with space
        var sizeLog2 = requestedSizeLog2
        while (newFreeLists[sizeLog2].isEmpty()) {
            sizeLog2++
        }

        // If we overshot, the free list we're currently pointing at is larger than
        // what we requested. Recursively split it in half until we get back to the
        // requested size.
        while (sizeLog2 > requestedSizeLog2) {
            val offset = newFreeLists[sizeLog2].first()
            newFreeLists[sizeLog2] = newFreeLists[sizeLog2].drop(1)
            sizeLog2--
            newFreeLists[sizeLog2] = listOf(offset, offset xor (1 shl sizeLog2))
        }

        // We know that list we want to use has at least one entry
        val allocatedOffset = newFreeLists[requestedSizeLog2].first()
        newFreeLists[requestedSizeLog2] = newFreeLists[requestedSizeLog2].drop(1)

        updateRootBlockData { r -> r.copy(freeLists = newFreeLists) }

        return BlockAddress(allocatedOffset, requestedSizeLog2)
    }

    fun releaseBlock(blockNumber: Int) {
        val address = getBlockAddress(blockNumber)
        releaseInner(address)

        val newBlockAddresses = rootBlockData.blockAddresses.toMutableList()
        newBlockAddresses[blockNumber] = null
        while (newBlockAddresses.isNotEmpty() && newBlockAddresses.last() == null) {
            newBlockAddresses.removeLast()
        }

        updateRootBlockData { r -> r.copy(blockAddresses = newBlockAddresses) }
    }

    private fun releaseInner(address: BlockAddress) {
        val newFreeLists = rootBlockData.freeLists.toMutableList()
        var offset = address.blockOffset
        var width = address.blockSizeLog2

        // Finds blocks which are immediately next to each other which can then be
        // removed from the free list they're on and recorded on the next free list up.
        // This occurs recursively until you hit the free list where there is no buddy block.
        while (true) {
            val (buddyOffset, buddyIndexInFreeList) = findNeighbour(newFreeLists, offset, width)
                ?: break

            val newFreeList = newFreeLists[width].toMutableList()
            newFreeList.removeAt(buddyIndexInFreeList)
            newFreeLists[width] = newFreeList

            offset = offset and buddyOffset
            width++
        }

        val newFreeList = newFreeLists[width].toMutableList()
        newFreeLists[width] = newFreeList

        val insertionPoint = newFreeList.binarySearch(offset)
        newFreeList.add(-insertionPoint - 1, offset)

        updateRootBlockData { r -> r.copy(freeLists = newFreeLists) }
    }

    /**
     * Finds the offset of the block immediately next door to the block offset
     * provided, given a block width.
     *
     * @param offset the offset of the known block.
     * @param width the width of the block.
     * @return `null` if not found, otherwise a pair of:
     *    - the block offset of the neighbour
     *    - the index of that block offset in the free list
     */
    private fun findNeighbour(freeLists: List<List<Int>>, offset: Int, width: Int): Pair<Int, Int>? {
        val freeList = freeLists[width]
        val neighbourOffset = offset xor (1 shl width)

        val indexInFreeList = freeList.indexOf(neighbourOffset)
        if (indexInFreeList < 0) {
            return null
        }

        return Pair(neighbourOffset, indexInFreeList)
    }

    private fun updateRootBlockData(modification: (RootBlockData) -> RootBlockData) {
        rootBlockData = modification(rootBlockData)
        dirty = true
    }

    /**
     * Reads a block from the buddy file.
     *
     * @param blockNumber the block number.
     * @return the block.
     */
    fun readBlock(blockNumber: Int): Block {
        val blockAddress = getBlockAddress(blockNumber)
        return stream.readBlock(blockAddress.blockOffset + 4L, blockAddress.blockSize)
    }

    /**
     * Writes a block to the buddy file.
     *
     * @param blockNumber the block number.
     * @param block the block to write.
     */
    fun writeBlock(blockNumber: Int, block: Block) {
        val blockAddress = getBlockAddress(blockNumber)
        writeBlock(blockAddress, block)
    }

    private fun writeBlock(blockAddress: BlockAddress, block: Block) {
        stream.writeBlock(blockAddress.blockOffset + 4L, block)
    }

    private fun getBlockAddress(blockNumber: Int): BlockAddress {
        if (blockNumber < 0 || blockNumber >= rootBlockData.blockAddresses.size) {
            throw BlockNotFoundException("Block $blockNumber is outside the range of block numbers")
        }
        return rootBlockData.blockAddresses[blockNumber]
            ?: throw BlockNotFoundException("Block $blockNumber is not allocated")
    }

    /**
     * Flushes unsaved changes.
     */
    fun flush() {
        if (dirty) {
            // Safely write root block at a new location, so it doesn't clobber the existing one
            // in case it fails part way through.
            // This is the one case where we can't just allocate a new block the normal way and
            // have to use the private methods - the root block must always be block 0.
            val newRootBlockSizeLog2 = BlockAddress.calculateMinimumSizeLog2(rootBlockData.calculateSize())
            val newRootBlockAddress = allocInner(newRootBlockSizeLog2)
            // Forced to call calculateSize() a second time because the reallocation can change the value
            val newRootBlockSize = rootBlockData.calculateSize()
            val newRootBlockOffset = newRootBlockAddress.blockOffset
            writeBlock(newRootBlockAddress, Block.create(newRootBlockSize) { stream ->
                rootBlockData.writeTo(stream)
            })
            releaseInner(BlockAddress(
                header.rootBlockOffset,
                BlockAddress.calculateMinimumSizeLog2(header.rootBlockSize)
            ))

            header = header.copy(
                rootBlockOffset = newRootBlockOffset,
                rootBlockSize = newRootBlockSize,
                rootBlockOffset2 = newRootBlockOffset
            )
            stream.writeBlock(4L, Block.create(BuddyHeader.SIZE) { stream ->
                header.writeTo(stream)
            })

            dirty = false
        }

        stream.flush()
    }

    override fun close() {
        flush()
        stream.close()
    }

    companion object {
        private const val FILE_MAGIC = 1

        /**
         * Opens a buddy file from the given path.
         *
         * @param path the path.
         * @param fileMode the mode to open the file in.
         */
        fun open(path: Path, fileMode: FileMode): BuddyFile {
            var success = false
            val channel = FileChannelIO.open(path, fileMode)
            try {
                if (channel.isEmpty && fileMode == FileMode.READ_WRITE) {
                    writeInitialEmptyFile(channel)
                }

                val result = BuddyFile(channel)
                success = true
                return result
            } finally {
                if (!success) {
                    channel.close()
                }
            }
        }

        // We have no idea what this means, but it was present in another file.
        private val UNKNOWN16_PLACEHOLDER = byteArrayOf(
            0, 0, 0x10, 0x0c,
            0, 0, 0, 0x87.toByte(),
            0, 0, 0x20, 0xb,
            0, 0, 0, 0
        )

        private fun writeInitialEmptyFile(channel: FileChannelIO) {
            val rootBlockOffset = 2048

            channel.writeBlock(0, Block.create(2048) { stream ->
                stream.writeInt(FILE_MAGIC)
                val header = BuddyHeader(
                    BuddyHeader.MAGIC,
                    rootBlockOffset,
                    1264,
                    rootBlockOffset,
                    Blob(UNKNOWN16_PLACEHOLDER)
                )
                header.writeTo(stream)
            })

            val rootBlockData = RootBlockData(
                // XXX: Could build this address from its parts, requires understanding the value
                listOf(BlockAddress(rootBlockOffset, 5)),
                mapOf(),
                buildList {
                    for (n in 0..4) {
                        add(listOf())
                    }
                    for (n in 5..10) {
                        add(listOf(1 shl n))
                    }
                    add(listOf())
                    for (n in 12..30) {
                        add(listOf(1 shl n))
                    }
                    add(listOf())
                }
            )

            channel.writeBlock(rootBlockOffset + 4L, Block.create(rootBlockData.calculateSize()) { stream ->
                rootBlockData.writeTo(stream)
            })

            channel.flush()
        }
    }
}