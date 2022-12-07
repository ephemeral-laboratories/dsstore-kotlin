package garden.ephemeral.macfiles.common.types

import garden.ephemeral.macfiles.common.io.Block
import java.nio.ByteBuffer

/**
 * An immutable byte array.
 */
class Blob(data: ByteArray) {
    private val data = data.copyOf()

    val size get() = data.size

    /**
     * Converts to a block, useful for performing further decoding of the value.
     *
     * @return the block.
     */
    fun toBlock(): Block {
        return Block(ByteBuffer.wrap(data))
    }

    /**
     * Converts to a byte array. Does so by copying the array.
     *
     * @return the byte array.
     */
    fun toByteArray() = data.copyOf()

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is Blob) return false
        return data.contentEquals(other.data)
    }

    override fun hashCode(): Int {
        return data.contentHashCode()
    }

    override fun toString(): String {
        return data.contentToString()
    }
}