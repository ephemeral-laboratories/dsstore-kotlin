package garden.ephemeral.macfiles.common.types

import java.nio.ByteBuffer

/**
 * Data class for a four-character-code.
 *
 * These are 4-char ASCII string values which are commonly used in Apple's formats.
 */
data class FourCC(val value: String) : Comparable<FourCC> {
    init {
        require(value.length == 4) { "FourCC must be length 4" }
        require(value.toByteArray().size == 4) { "FourCC must contain only ASCII" }
    }

    override fun compareTo(other: FourCC): Int {
        return value.compareTo(other.value)
    }

    companion object {
        val ZERO: FourCC = FourCC("\u0000\u0000\u0000\u0000")

        fun fromInt(value: Int): FourCC {
            val array = ByteArray(4)
            val buffer = ByteBuffer.wrap(array)
            buffer.putInt(value)
            return FourCC(array.decodeToString())
        }
    }
}
