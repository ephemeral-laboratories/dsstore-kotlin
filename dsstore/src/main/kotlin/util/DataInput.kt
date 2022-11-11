package util

import types.Blob
import types.FourCC
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
     * Reads a 4-byte big endian integer.
     *
     * @return the int.
     */
    fun readInt(): Int

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
}
