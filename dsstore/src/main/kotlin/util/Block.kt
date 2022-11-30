package util

import types.Blob
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * A block of data in the file.
 */
class Block(private val buffer: ByteBuffer) : DataInput by ByteBufferDataInput(buffer) {
    val size get() = buffer.capacity()

    init {
        buffer.order(ByteOrder.BIG_ENDIAN)
    }

    /**
     * Duplicates the buffer so the caller can read it directly
     * without upsetting the internal state.
     * Also resets the position of the buffer.
     *
     * @return the buffer.
     */
    fun duplicateBuffer(): ByteBuffer {
        return buffer.duplicate().position(0)
    }

    /**
     * Convenience method to convert to a blob.
     *
     * @return the blob.
     */
    fun toBlob(): Blob {
        val bufferCopy = duplicateBuffer()
        val data = ByteArray(bufferCopy.remaining())
        bufferCopy.get(data)
        return Blob(data)
    }

    companion object {
        /**
         * Creates a new block by allocating a buffer and writing provided data into it.
         *
         * @param size the size to allocate for the buffer.
         * @param writeLogic a callback called to write the data.
         * @return the block.
         */
        fun create(size: Int, writeLogic: (DataOutput) -> Unit): Block {
            val buffer = ByteBuffer.allocate(size)
            writeLogic(ByteBufferDataOutput(buffer))
            buffer.flip()
            return Block(buffer)
        }
    }
}