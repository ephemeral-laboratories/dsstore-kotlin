package codecs

import util.DataInput

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

// Later, when we add write support:
//    fun encode(value: T, stream: DataOutput)
}
