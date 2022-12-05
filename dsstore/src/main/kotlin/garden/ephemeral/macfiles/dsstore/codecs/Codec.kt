package garden.ephemeral.macfiles.dsstore.codecs

import garden.ephemeral.macfiles.dsstore.util.DataInput
import garden.ephemeral.macfiles.dsstore.util.DataOutput

/**
 * Abstraction of a codec for encoding and decoding blobs for specific properties.
 */
interface Codec<T> {

    /**
     * Decodes the property value.
     *
     * @param stream a stream opened over the encoded value.
     * @return the decoded value.
     */
    fun decode(stream: DataInput): T

    /**
     * Calculates the size required to store the value.
     *
     * @param value the value.
     * @return the size of the value, in bytes.
     */
    fun calculateSize(value: T): Int

    /**
     * Encodes the property value.
     *
     * @param value the value.
     * @param stream a stream opened for writing the new value.
     */
    fun encode(value: T, stream: DataOutput)
}
