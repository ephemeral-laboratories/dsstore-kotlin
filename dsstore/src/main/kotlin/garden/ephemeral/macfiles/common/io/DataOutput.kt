package garden.ephemeral.macfiles.common.io

import garden.ephemeral.macfiles.common.types.Blob
import garden.ephemeral.macfiles.common.types.FourCC
import java.nio.charset.Charset

/**
 * Abstraction of data output, passed into various methods for writing data.
 */
interface DataOutput {

    /**
     * Gets the position of the stream.
     *
     * @return the position.
     */
    fun position(): Int

    /**
     * Sets the position of the stream. (i.e., seeks.)
     *
     * @param position the new position.
     */
    fun position(position: Int)

    /**
     * Skips the given number of bytes.
     *
     * @param byteCount the number of bytes to skip.
     */
    fun skip(byteCount: Int)

    /**
     * Writes a single byte.
     *
     * @param value the byte.
     */
    fun writeByte(value: Byte)

    /**
     * Writes a 2-byte big endian integer.
     *
     * @param value the short.
     */
    fun writeShort(value: Short)

    /**
     * Writes a 4-byte big endian integer.
     *
     * @param value the int.
     */
    fun writeInt(value: Int)

    /**
     * Writes a 4-byte little endian integer.
     *
     * @param value the int.
     */
    fun writeIntLE(value: Int)

    /**
     * Writes a 4-byte big endian unsigned integer.
     *
     * @param value the int.
     */
    fun writeUInt(value: UInt)

    /**
     * Writes an 8-byte big endian integer.
     *
     * @param value the long.
     */
    fun writeLong(value: Long)

    /**
     * Writes an 8-byte *little* endian integer.
     *
     * @param value the long.
     */
    fun writeLongLE(value: Long)

    /**
     * Writes a four-character-code.
     *
     * @param value the fourcc.
     */
    fun writeFourCC(value: FourCC)

    /**
     * Writes a blob.
     *
     * @param value the blob.
     */
    fun writeBlob(value: Blob)

    /**
     * Writes a string.
     *
     * @param value the string.
     * @param charset the charset to use to encode the string.
     */
    fun writeString(value: String, charset: Charset)

    /**
     * Writes a string. The string is padded out to the given length with null bytes.
     *
     * @param length the length to write.
     * @param value the string.
     * @param charset the charset to use to encode the string.
     */
    fun writeString(length: Int, value: String, charset: Charset)

    /**
     * Writes a "Pascal string". The first byte written is the length of the string,
     * and the string is padded out to the given length with null bytes.
     *
     * @param length the length to write.
     * @param value the string to write.
     * @param charset the charset to use to encode the string.
     */
    fun writePascalString(length: Int, value: String, charset: Charset)
}
