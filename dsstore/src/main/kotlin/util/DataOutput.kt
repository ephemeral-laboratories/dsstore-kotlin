package util

import types.Blob
import types.FourCC
import java.nio.charset.Charset

/**
 * Abstraction of data output, passed into various methods for writing data.
 */
interface DataOutput {

    /**
     * Writes a single byte.
     *
     * @param value the byte.
     */
    fun writeByte(value: Byte)

    /**
     * Writes a 4-byte big endian integer.
     *
     * @param value the int.
     */
    fun writeInt(value: Int)

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
}
