package buddy

import types.Blob
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
            channel.writeBlock(0, Block.create(2048) { stream ->
                stream.writeInt(FILE_MAGIC)
                val header = BuddyHeader(BuddyHeader.MAGIC, 2048, 1264, 2048, Blob(UNKNOWN16_PLACEHOLDER))
                header.writeTo(stream)
            })

            val rootBlockOffset = 2048

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