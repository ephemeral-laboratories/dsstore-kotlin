package garden.ephemeral.macfiles.dsstore.codecs

import garden.ephemeral.macfiles.common.types.Blob

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
    fun decode(blob: Blob): T

    /**
     * Encodes the property value.
     *
     * @param value the value.
     * @return a blob containing the encoded value.
     */
    fun encode(value: T): Blob
}
