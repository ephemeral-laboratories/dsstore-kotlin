package types

import util.Block
import java.nio.ByteBuffer

/**
 * An immutable byte array.
 */
class Blob(private val data: ByteArray) {
    val size get() = data.size

    /**
     * Converts to a block, useful for performing further decoding of the value.
     *
     * @return the block.
     */
    fun toBlock(): Block {
        return Block(ByteBuffer.wrap(data))
    }

    override fun equals(other: Any?): Boolean {
        if (other == this) return true
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