package garden.ephemeral.macfiles.dsstore.util

import garden.ephemeral.macfiles.common.io.Block
import java.io.Closeable
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.Path

/**
 * Wrapper around a file channel to provide a convenience method for reading blocks from the file.
 */
class FileChannelIO(private val channel: FileChannel) : Closeable {
    val isEmpty get() = channel.size() == 0L

    /**
     * Reads a block from the file.
     *
     * @param offset the offset from the start of the file, in bytes.
     * @param length the length to read, in bytes.
     * @return the block.
     */
    fun readBlock(offset: Long, length: Int): Block {
        val buffer = ByteBuffer.allocate(length)
        channel.position(offset)
        while (buffer.hasRemaining()) {
            if (channel.read(buffer) < 0) {
                break
            }
        }
        buffer.rewind()
        return Block(buffer)
    }

    /**
     * Writes a block to the file.
     *
     * @param offset the offset from the start of the file, in bytes.
     * @param block the block to write.
     */
    fun writeBlock(offset: Long, block: Block) {
        channel.position(offset)
        val buffer = block.duplicateBuffer()
        while (buffer.hasRemaining()) {
            channel.write(buffer)
        }
    }

    /**
     * Flushes updates to the file.
     */
    fun flush() {
        channel.force(true)
    }

    override fun close() {
        flush()
        channel.close()
    }

    companion object {

        /**
         * Opens a file.
         *
         * @param path the path to the file.
         * @param fileMode the mode to open the file in.
         * @return the file channel I/O wrapper.
         */
        fun open(path: Path, fileMode: FileMode): FileChannelIO {
            val channel = FileChannel.open(path, fileMode.toOpenOptions())
            return FileChannelIO(channel)
        }
    }
}