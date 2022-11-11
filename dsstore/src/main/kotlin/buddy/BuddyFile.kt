package buddy

import util.Block
import util.FileChannelIO
import util.FileMode
import java.io.Closeable
import java.nio.file.Path

/**
 * Represents the entire buddy file.
 */
class BuddyFile(private val stream: FileChannelIO) : Closeable {
    private val rootBlockData: RootBlockData

    init {
        val headersBlock = stream.readBlock(0, 36)
        val magic = headersBlock.readInt()
        require(magic == FILE_MAGIC) { "File magic $magic not expected $FILE_MAGIC" }

        val header = BuddyHeader.readFrom(headersBlock)

        val rootBlock = stream.readBlock(header.rootBlockOffset + 4L, header.rootBlockSize)
        rootBlockData = RootBlockData.readFrom(rootBlock)
    }

    /**
     * Gets the table of contents.
     */
    val toc get() = rootBlockData.tocEntries

    /**
     * Reads a block from the buddy file.
     *
     * @param blockNumber the block number.
     * @return the block.
     */
    fun readBlock(blockNumber: Int): Block {
        if (blockNumber < 0 || blockNumber >= rootBlockData.blockAddresses.size) {
            throw BlockNotFoundException("Block $blockNumber is outside the range of block numbers")
        }
        val blockAddress = rootBlockData.blockAddresses[blockNumber]
            ?: throw BlockNotFoundException("Block $blockNumber is not allocated")
        return stream.readBlock(blockAddress.blockOffset + 4L, blockAddress.blockSize)
    }

    override fun close() {
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
            return BuddyFile(FileChannelIO.open(path, fileMode))
        }
    }
}