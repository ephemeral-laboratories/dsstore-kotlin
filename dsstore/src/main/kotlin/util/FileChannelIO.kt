package util

import java.io.Closeable
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.Path

/**
 * Wrapper around a file channel to provide a convenience method for reading blocks from the file.
 */
class FileChannelIO(private val channel: FileChannel) : Closeable {

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
        buffer.flip()
        return Block(buffer)
    }

    override fun close() {
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