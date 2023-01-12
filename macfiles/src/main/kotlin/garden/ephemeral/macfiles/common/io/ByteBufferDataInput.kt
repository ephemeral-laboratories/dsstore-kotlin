package garden.ephemeral.macfiles.common.io

import garden.ephemeral.macfiles.common.types.Blob
import garden.ephemeral.macfiles.common.types.FourCC
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

/**
 * Adapts a [ByteBuffer] as a [DataInput].
 */
class ByteBufferDataInput(private val buffer: ByteBuffer) : DataInput {
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

    override fun readByte(): Byte {
        requireAvailable(1)
        return buffer.get()
    }

    override fun readShort(): Short {
        requireAvailable(2)
        return buffer.short
    }

    override fun readShortLE(): Short {
        requireAvailable(2)
        val result = buffer.duplicate().order(ByteOrder.LITTLE_ENDIAN).short
        skip(2)
        return result
    }

    override fun readInt(): Int {
        requireAvailable(4)
        return buffer.int
    }

    override fun readIntLE(): Int {
        requireAvailable(4)
        val result = buffer.duplicate().order(ByteOrder.LITTLE_ENDIAN).int
        skip(4)
        return result
    }

    override fun readUInt() = readInt().toUInt()

    override fun readLong(): Long {
        requireAvailable(8)
        return buffer.long
    }

    override fun readLongLE(): Long {
        requireAvailable(8)
        val result = buffer.duplicate().order(ByteOrder.LITTLE_ENDIAN).long
        skip(8)
        return result
    }

    override fun readFloat(): Float {
        requireAvailable(4)
        return buffer.float
    }

    override fun readFloatLE(): Float {
        requireAvailable(4)
        val result = buffer.duplicate().order(ByteOrder.LITTLE_ENDIAN).float
        skip(4)
        return result
    }

    override fun readDouble(): Double {
        requireAvailable(8)
        return buffer.double
    }

    override fun readDoubleLE(): Double {
        requireAvailable(8)
        val result = buffer.duplicate().order(ByteOrder.LITTLE_ENDIAN).double
        skip(8)
        return result
    }

    override fun readFourCC(): FourCC {
        return FourCC(readArray(4).toString(StandardCharsets.US_ASCII))
    }

    override fun readBlob(lengthBytes: Int): Blob {
        return Blob(readArray(lengthBytes))
    }

    override fun readString(lengthBytes: Int, charset: Charset): String {
        return readArray(lengthBytes).toString(charset)
    }

    override fun readPascalString(lengthBytes: Int, charset: Charset): String {
        requireAvailable(lengthBytes)
        val length = readByte().toUByte().toInt()
        return readArray(lengthBytes - 1).copyOf(length).toString(charset)
    }

    private fun readArray(lengthBytes: Int): ByteArray {
        requireAvailable(lengthBytes)
        val array = ByteArray(lengthBytes)
        buffer.get(array)
        return array
    }

    private fun requireAvailable(byteCount: Int) {
        require(byteCount <= buffer.remaining()) {
            "Requested byte count $byteCount exceeds remaining bytes ${buffer.remaining()}"
        }
    }
}