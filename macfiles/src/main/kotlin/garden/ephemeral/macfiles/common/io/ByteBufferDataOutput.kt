package garden.ephemeral.macfiles.common.io

import garden.ephemeral.macfiles.common.types.Blob
import garden.ephemeral.macfiles.common.types.FourCC
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

/**
 * Adapts a [ByteBuffer] as a [DataOutput].
 */
class ByteBufferDataOutput(private val buffer: ByteBuffer) : DataOutput {
    override fun position(): Int {
        return buffer.position()
    }

    override fun position(position: Int) {
        buffer.position(position)
    }

    override fun skip(byteCount: Int) {
        requireAvailable(byteCount)
        buffer.position(buffer.position() + byteCount)
    }

    override fun writeByte(value: Byte) {
        requireAvailable(1)
        buffer.put(value)
    }

    override fun writeShort(value: Short) {
        requireAvailable(2)
        buffer.putShort(value)
    }

    override fun writeShortLE(value: Short) {
        requireAvailable(2)
        buffer.duplicate().order(ByteOrder.LITTLE_ENDIAN).putShort(value)
        buffer.position(buffer.position() + 2)
    }

    override fun writeInt(value: Int) {
        requireAvailable(4)
        buffer.putInt(value)
    }

    override fun writeIntLE(value: Int) {
        requireAvailable(4)
        buffer.duplicate().order(ByteOrder.LITTLE_ENDIAN).putInt(value)
        buffer.position(buffer.position() + 4)
    }

    override fun writeUInt(value: UInt) = writeInt(value.toInt())

    override fun writeLong(value: Long) {
        requireAvailable(8)
        buffer.putLong(value)
    }

    override fun writeLongLE(value: Long) {
        requireAvailable(8)
        buffer.duplicate().order(ByteOrder.LITTLE_ENDIAN).putLong(value)
        buffer.position(buffer.position() + 8)
    }

    override fun writeFloat(value: Float) {
        requireAvailable(4)
        buffer.putFloat(value)
    }

    override fun writeFloatLE(value: Float) {
        requireAvailable(4)
        buffer.duplicate().order(ByteOrder.LITTLE_ENDIAN).putFloat(value)
        buffer.position(buffer.position() + 4)
    }

    override fun writeDouble(value: Double) {
        requireAvailable(8)
        buffer.putDouble(value)
    }

    override fun writeDoubleLE(value: Double) {
        requireAvailable(8)
        buffer.duplicate().order(ByteOrder.LITTLE_ENDIAN).putDouble(value)
        buffer.position(buffer.position() + 8)
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

    override fun writeString(length: Int, value: String, charset: Charset) {
        var array = value.toByteArray(charset)
        array = array.copyOf(length)
        writeArray(array)
    }

    override fun writePascalString(length: Int, value: String, charset: Charset) {
        var array = value.toByteArray(charset)
        if (array.size > length - 1) {
            array = array.copyOf(length - 1)
        }
        val valueLength = array.size
        array = array.copyOf(length - 1)
        writeByte(valueLength.toByte())
        writeArray(array)
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