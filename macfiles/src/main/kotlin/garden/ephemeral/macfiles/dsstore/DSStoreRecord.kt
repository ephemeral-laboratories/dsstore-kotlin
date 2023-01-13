package garden.ephemeral.macfiles.dsstore
import garden.ephemeral.macfiles.common.io.DataInput
import garden.ephemeral.macfiles.common.io.DataOutput
import garden.ephemeral.macfiles.common.types.Blob
import garden.ephemeral.macfiles.common.types.FourCC
import garden.ephemeral.macfiles.dsstore.codecs.PropertyCodecs
import java.nio.charset.StandardCharsets

/**
 * One record in a DS_Store file.
 *
 * @property filename the filename this record is storing information for.
 * @property propertyId the property ID indicating what is being stored.
 * @property typeId the type ID indicating what kind of value follows.
 * @property value the value as read from the stream. Only partial interpretation
 *           is performed at read-time, and advanced decoding of blobs is done
 *           on-demand.
 */
data class DSStoreRecord(
    val filename: String,
    val propertyId: FourCC,
    val typeId: DSStoreValueType,
    val value: Any,
) {
    constructor(filename: String, propertyId: FourCC, value: Any) :
            this(filename, propertyId, encodeValue(propertyId, value))

    constructor(filename: String, propertyId: FourCC, encodedValue: Pair<DSStoreValueType, Any>) :
            this(filename, propertyId, encodedValue.first, encodedValue.second)

    /**
     * Extracts just the key from the record.
     *
     * @return the key.
     */
    fun extractKey() = DSStoreRecordKey(filename, propertyId)

    /**
     * Compares the record against a key.
     *
     * @param key the key to compare with.
     * @return the result of the comparison. Same semantics as [Comparable.compareTo].
     */
    fun compareToKey(key: DSStoreRecordKey): Int {
        val comp = filename.compareTo(key.filename, ignoreCase = true)
        if (comp != 0) return comp

        return propertyId.compareTo(key.propertyId)
    }

    /**
     * Decodes the stored value.
     *
     * If the value is a blob, looks up a repository of codecs which tell us how
     * to decode that specific blob. Non-blob values are left alone.
     *
     * The decoding of values is deliberately left until later, to avoid unnecessary
     * decoding of values which will not be used.
     *
     * @return the decoded value.
     */
    fun decodeValue(): Any {
        if (value !is Blob) return value

        val codec = PropertyCodecs.findCodec(propertyId) ?: return value

        return codec.decode(value)
    }

    /**
     * Calculates the space required to encode this record.
     *
     * @return the size of this record in bytes.
     */
    fun calculateSize() = 12 + filename.length * 2 + typeId.calculateSize(value)

    /**
     * Writes the record to a stream.
     *
     * @param stream the stream to write to.
     */
    fun writeTo(stream: DataOutput) {
        stream.writeInt(filename.length)
        stream.writeString(filename, StandardCharsets.UTF_16BE)
        stream.writeFourCC(propertyId)
        stream.writeFourCC(typeId.typeId)
        typeId.writeValue(value, stream)
    }


    companion object {
        private fun encodeValue(propertyId: FourCC, value: Any): Pair<DSStoreValueType, Any> {
            // If the value is any of the built-in types, just leave it as-is.
            // This includes blobs provided as Blob.
            val typeId = DSStoreValueType.findForValue(value)
            if (typeId != null) {
                return Pair(typeId, value)
            }

            // For everything else, look up a codec
            val codec = PropertyCodecs.findCodec(propertyId)
                ?: throw IllegalArgumentException("No codec for value: $value")

            return Pair(DSStoreValueType.BLOB, codec.encode(value))
        }

        /**
         * Reads the record from a stream.
         *
         * @param stream the stream to read from.
         * @return the read record.
         */
        fun readFrom(stream: DataInput): DSStoreRecord {
            val filenameLength = stream.readInt()
            val filename = stream.readString(filenameLength * 2, StandardCharsets.UTF_16BE)
            val propertyId = stream.readFourCC()
            val typeId = DSStoreValueType.forTypeId(stream.readFourCC())
            val value = typeId.readValue(stream)
            return DSStoreRecord(filename, propertyId, typeId, value)
        }
    }
}