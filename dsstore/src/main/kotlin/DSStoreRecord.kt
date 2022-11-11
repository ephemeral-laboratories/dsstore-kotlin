import codecs.PropertyCodecs
import types.Blob
import types.FourCC
import util.DataInput
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
    val typeId: FourCC,
    val value: Any,
) {
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

        return codec.decode(value.toBlock())
    }

    companion object {

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
            val typeId = stream.readFourCC()
            val value = DSStoreValueType.forTypeId(typeId).readValue(stream)
            return DSStoreRecord(filename, propertyId, typeId, value)
        }
    }
}