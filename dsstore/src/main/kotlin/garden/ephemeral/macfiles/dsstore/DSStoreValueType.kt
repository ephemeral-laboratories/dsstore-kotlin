package garden.ephemeral.macfiles.dsstore
import garden.ephemeral.macfiles.common.MacTimeUtils
import garden.ephemeral.macfiles.common.io.DataInput
import garden.ephemeral.macfiles.common.io.DataOutput
import garden.ephemeral.macfiles.common.types.Blob
import garden.ephemeral.macfiles.common.types.FourCC
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime

/**
 * Enumeration of value types found in `.DS_Store` files.
 *
 * @property typeId the type ID as stored in the file.
 */
enum class DSStoreValueType(val typeId: FourCC) {
    /**
     * An integer (4 bytes)
     */
    INT(FourCC("long")) {
        override fun readValue(stream: DataInput) = stream.readInt()
        override fun writeValue(value: Any, stream: DataOutput) = stream.writeInt(value as Int)
        override fun calculateSize(value: Any) = 4
    },

    /**
     * A short integer? Still stored as four bytes, but the first two are always zero.
     */
    SHORT(FourCC("shor")) {
        override fun readValue(stream: DataInput) = stream.readInt().toShort()
        override fun writeValue(value: Any, stream: DataOutput) = stream.writeInt((value as Short).toInt())
        override fun calculateSize(value: Any) = 4
    },

    /**
     * A boolean value, stored as one byte.
     */
    BOOL(FourCC("bool")) {
        override fun readValue(stream: DataInput) = stream.readByte() != 0.toByte()
        override fun writeValue(value: Any, stream: DataOutput) = stream.writeByte(if (value as Boolean) 1 else 0)
        override fun calculateSize(value: Any) = 1
    },

    /**
     * Four bytes, containing a FourCharCode.
     */
    TYPE(FourCC("type")) {
        override fun readValue(stream: DataInput) = stream.readFourCC()
        override fun writeValue(value: Any, stream: DataOutput) = stream.writeFourCC(value as FourCC)
        override fun calculateSize(value: Any) = 4
    },

    /**
     * An arbitrary block of bytes, stored as an integer followed by that many bytes of data.
     */
    BLOB(FourCC("blob")) {
        override fun readValue(stream: DataInput): Any {
            val length = stream.readInt()
            return stream.readBlob(length)
        }

        override fun writeValue(value: Any, stream: DataOutput) {
            value as Blob
            stream.writeInt(value.size)
            stream.writeBlob(value)
        }

        override fun calculateSize(value: Any) = 4 + (value as Blob).size
    },

    /**
     * A Unicode text string, stored as an integer character count followed by 2*count bytes of data in UTF-16.
     */
    UNICODE_STRING(FourCC("ustr")) {
        override fun readValue(stream: DataInput): Any {
            val length = stream.readInt()
            return stream.readString(length * 2, StandardCharsets.UTF_16BE)
        }

        override fun writeValue(value: Any, stream: DataOutput) {
            value as String
            stream.writeInt(value.length)
            stream.writeString(value, StandardCharsets.UTF_16BE)
        }

        override fun calculateSize(value: Any) = 4 + (value as String).length * 2
    },

    /**
     * An eight-byte (64-bit) integer. (I don't know why the abbreviation "comp" was chosen for this.)
     */
    LONG(FourCC("comp")) {
        override fun readValue(stream: DataInput) = stream.readLong()
        override fun writeValue(value: Any, stream: DataOutput) = stream.writeLong(value as Long)
        override fun calculateSize(value: Any) = 8
    },

    /**
     * A datestamp, represented as an 8-byte integer count of the number of (1/65536)-second intervals since
     * the Mac epoch in 1904. Given the name, this probably corresponds to the UTCDateTime structure.
     */
    DATE_UTC(FourCC("dutc")) {
        override fun readValue(stream: DataInput): Any {
            val value = stream.readLong()
            return MacTimeUtils.decodeHighResInstant(value)
        }

        override fun writeValue(value: Any, stream: DataOutput) {
            val encoded = MacTimeUtils.encodeHighResInstant(value as Instant)
            stream.writeLong(encoded)
        }

        override fun calculateSize(value: Any) = 8
    },

    ;

    /**
     * Reads the value from a stream.
     *
     * @param stream the stream.
     * @return the read value.
     */
    abstract fun readValue(stream: DataInput): Any

    /**
     * Writes the value to a stream.
     *
     * @param value the value to write.
     * @param stream the stream.
     */
    abstract fun writeValue(value: Any, stream: DataOutput)

    /**
     * Calculates the encoded size of a value.
     *
     * @param value the value.
     * @return the size in bytes.
     */
    abstract fun calculateSize(value: Any): Int

    companion object {
        private val MacEpoch = ZonedDateTime.of(1904, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)
        private const val MacDateDivisor = 65536

        private val byTypeId by lazy {
            buildMap {
                DSStoreValueType.values().forEach { type ->
                    put(type.typeId, type)
                }
            }
        }

        /**
         * Looks up a value type by its ID.
         *
         * @param typeId the ID to look up.
         * @return the value type.
         * @throws IllegalArgumentException if the type is invalid.
         */
        fun forTypeId(typeId: FourCC): DSStoreValueType {
            return byTypeId[typeId] ?: throw IllegalArgumentException("Unknown type ID: $typeId")
        }

        /**
         * Tries to find a type which can be used for the given value.
         *
         * @param value the value.
         * @return the value type, or `null` if none matched.
         */
        fun findForValue(value: Any): DSStoreValueType? {
            return when (value) {
                is Int -> INT
                is Short -> SHORT
                is Boolean -> BOOL
                is FourCC -> TYPE
                is Blob -> BLOB
                is String -> UNICODE_STRING
                is Long -> LONG
                is Instant -> DATE_UTC
                else -> null
            }
        }
    }
}