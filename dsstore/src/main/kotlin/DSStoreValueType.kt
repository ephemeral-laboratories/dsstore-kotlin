
import types.FourCC
import util.DataInput
import java.nio.charset.StandardCharsets
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
    },

    /**
     * A short integer? Still stored as four bytes, but the first two are always zero.
     */
    SHORT(FourCC("shor")) {
        override fun readValue(stream: DataInput) = stream.readInt().toShort()
    },

    /**
     * A boolean value, stored as one byte.
     */
    BOOL(FourCC("bool")) {
        override fun readValue(stream: DataInput) = stream.readByte() != 0.toByte()
    },

    /**
     * Four bytes, containing a FourCharCode.
     */
    TYPE(FourCC("type")) {
        override fun readValue(stream: DataInput) = stream.readFourCC()
    },

    /**
     * An arbitrary block of bytes, stored as an integer followed by that many bytes of data.
     */
    BLOB(FourCC("blob")) {
        override fun readValue(stream: DataInput): Any {
            val length = stream.readInt()
            return stream.readBlob(length)
        }
    },

    /**
     * A Unicode text string, stored as an integer character count followed by 2*count bytes of data in UTF-16.
     */
    UNICODE_STRING(FourCC("ustr")) {
        override fun readValue(stream: DataInput): Any {
            val length = stream.readInt()
            return stream.readString(length * 2, StandardCharsets.UTF_16BE)
        }
    },

    /**
     * An eight-byte (64-bit) integer. (I don't know why the abbreviation "comp" was chosen for this.)
     */
    LONG(FourCC("comp")) {
        override fun readValue(stream: DataInput) = stream.readLong()
    },

    /**
     * A datestamp, represented as an 8-byte integer count of the number of (1/65536)-second intervals since
     * the Mac epoch in 1904. Given the name, this probably corresponds to the UTCDateTime structure.
     */
    DATE_UTC(FourCC("dutc")) {
        override fun readValue(stream: DataInput): Any {
            val value = stream.readLong()
            return MacEpoch
                .plusSeconds(value / MacDateDivisor)
                .plusNanos(1_000_000_000L * (value % MacDateDivisor) / MacDateDivisor)
                .toInstant()
        }
    },

    ;

    /**
     * Reads the value from a stream.
     *
     * @param stream the stream.
     * @return the read value.
     */
    abstract fun readValue(stream: DataInput): Any

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
    }
}