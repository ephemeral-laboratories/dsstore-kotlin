package garden.ephemeral.macfiles.common.io

import garden.ephemeral.macfiles.common.types.Blob
import garden.ephemeral.macfiles.common.types.FourCC
import java.nio.charset.Charset

/**
 * Abstraction of data input, passed into various methods for reading data.
 */
interface DataInput {

    /**
     * Skips the given number of bytes.
     *
     * @param byteCount the number of bytes to skip.
     */
    fun skip(byteCount: Int)

    /**
     * Reads a single byte.
     *
     * @return the byte.
     */
    fun readByte(): Byte

    /**
     * Reads a 2-byte big endian integer.
     *
     * @return the short.
     */
    fun readShort(): Short

    /**
     * Reads a 4-byte big endian integer.
     *
     * @return the int.
     */
    fun readInt(): Int

    /**
     * Reads a 4-byte big endian unsigned integer.
     *
     * @return the uint.
     */
    fun readUInt(): UInt

    /**
     * Reads an 8-byte big endian integer.
     *
     * @return the long.
     */
    fun readLong(): Long

    /**
     * Reads an 8-byte *little* endian integer.
     *
     * @return the long.
     */
    fun readLongLE(): Long

    /**
     * Reads a four-character-code.
     *
     * @return the fourcc.
     */
    fun readFourCC(): FourCC

    /**
     * Reads a blob.
     *
     * @param lengthBytes the length to read, in bytes.
     * @return the blob.
     */
    fun readBlob(lengthBytes: Int): Blob

    /**
     * Reads a string.
     *
     * @param lengthBytes the length to read, in bytes.
     * @param charset the charset to use to decode the string.
     * @return the string.
     */
    fun readString(lengthBytes: Int, charset: Charset): String

    /**
     * Reads a "Pascal string". The fixed number of bytes are read, and
     * then the first byte is used as the length of the actual string to return.
     *
     * @param lengthBytes the length to read, in bytes.
     * @param charset the charset to use to decode the string.
     * @return the string.
     */
    fun readPascalString(lengthBytes: Int, charset: Charset): String
}
