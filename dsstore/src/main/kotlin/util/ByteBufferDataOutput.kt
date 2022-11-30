package util

import types.Blob
import types.FourCC
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

/**
 * Adapts a [ByteBuffer] as a [DataOutput].
 */
class ByteBufferDataOutput(private val buffer: ByteBuffer) : DataOutput {
    override fun skip(byteCount: Int) {
        requireAvailable(byteCount)
        buffer.position(buffer.position() + byteCount)
    }

    override fun writeByte(value: Byte) {
        requireAvailable(1)
        buffer.put(value)
    }

    override fun writeInt(value: Int) {
        requireAvailable(4)
        buffer.putInt(value)
    }

    override fun writeLong(value: Long) {
        requireAvailable(8)
        buffer.putLong(value)
    }

    override fun writeLongLE(value: Long) {
        requireAvailable(8)
        buffer.duplicate().order(ByteOrder.LITTLE_ENDIAN).putLong(value)
    }

    override fun writeFourCC(value: FourCC) {
        writeArray(value.value.toByteArray(StandardCharsets.US_ASCII))
    }

    override fun writeBlob(value: Blob) {
        writeArray(value.toByteArray())
    }

    override fun writeString(value: String, charset: Charset) {
        writeArray(value.toByteArray(charset))
    }

    private fun writeArray(array: ByteArray) {
        requireAvailable(array.size)
        buffer.put(array)
    }

    private fun requireAvailable(byteCount: Int) {
        require(byteCount <= buffer.remaining()) {
            "Requested byte count $byteCount exceeds remaining bytes ${buffer.remaining()}"
        }
    }
}