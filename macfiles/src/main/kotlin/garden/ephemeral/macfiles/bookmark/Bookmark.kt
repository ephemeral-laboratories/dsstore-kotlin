package garden.ephemeral.macfiles.bookmark

import garden.ephemeral.macfiles.bookmark.types.URL
import garden.ephemeral.macfiles.bookmark.types.UUID
import garden.ephemeral.macfiles.bookmark.types.Unrecognised
import garden.ephemeral.macfiles.common.MacTimeUtils
import garden.ephemeral.macfiles.common.io.DataInput
import garden.ephemeral.macfiles.common.io.DataOutput
import garden.ephemeral.macfiles.common.types.Blob
import java.nio.charset.StandardCharsets

/**
 * Representation of a macOS bookmark file.
 */
data class Bookmark(
    val tocs: Map<Int, Map<TocKey, Any?>>
) {
    operator fun get(key: TocKey): Any? {
        for (toc in tocs.values) {
            if (toc.containsKey(key)) {
                return toc[key]
            }
        }
        throw IllegalArgumentException("Key not found: $key")
    }

    // TODO: Figure out how mutation works for this. Ideally it would be immutable, like Alias.
//    operator fun set(key: TocKey, value: Nothing) {
//        if (tocs.isEmpty()) {
//            tocs[1] = mutableMapOf<TocKey, Nothing>()
//        }
//        tocs.values.first()[key] = value
//    }

    fun toBlob(): Blob {
        TODO()
    }

    fun writeTo(stream: DataOutput) {
        TODO()
    }

    /*
    def to_bytes(self):
        """Convert this :class:`Bookmark` to a byte representation."""

        result = []
        tocs = []
        offset = 4  # For the offset to the first TOC

        # Generate the data and build the TOCs
        for tid, toc in self.tocs:
            entries = []

            for k, v in toc.items():
                if isinstance(k, str):
                    noffset = offset
                    voffset, enc = self._encode_item(k, offset)
                    result.append(enc)
                    offset, enc = self._encode_item(v, voffset)
                    result.append(enc)
                    entries.append((noffset | 0x80000000, voffset))
                else:
                    entries.append((k, offset))
                    offset, enc = self._encode_item(v, offset)
                    result.append(enc)

            # TOC entries must be sorted - CoreServicesInternal does a
            # binary search to find data
            entries.sort()

            tocs.append(
                (tid, b"".join([struct.pack(b"<III", k, o, 0) for k, o in entries]))
            )

        first_toc_offset = offset

        # Now generate the TOC headers
        for ndx, toc in enumerate(tocs):
            tid, data = toc
            if ndx == len(tocs) - 1:
                next_offset = 0
            else:
                next_offset = offset + 20 + len(data)

            result.append(
                struct.pack(
                    b"<IIIII",
                    len(data) - 8,
                    0xFFFFFFFE,
                    tid,
                    next_offset,
                    len(data) // 12,
                    )
            )
            result.append(data)

            offset += 20 + len(data)

        # Finally, add the header (and the first TOC offset, which isn't part
        # of the header, but goes just after it)
        header = struct.pack(
            b"<4sIIIQQQQI",
            b"book",
            offset + 48,
            0x10040000,
            48,
            0,
            0,
            0,
            0,
            first_toc_offset,
            )

        result.insert(0, header)

        return b"".join(result)
     */

    companion object {
        private const val DATA_TYPE_MASK = 0xFFFFFF00.toInt()
        private const val DATA_SUBTYPE_MASK = 0x000000FF

        private const val TYPE_STRING = 0x0100
        private const val TYPE_DATA = 0x0200
        private const val TYPE_NUMBER = 0x0300
        private const val TYPE_DATE = 0x0400
        private const val TYPE_BOOLEAN = 0x0500
        private const val TYPE_ARRAY = 0x0600
        private const val TYPE_DICT = 0x0700
        private const val TYPE_UUID = 0x0800
        private const val TYPE_URL = 0x0900
        private const val TYPE_NULL = 0x0A00

        private const val SUBTYPE_ZERO = 0x0000
        private const val SUBTYPE_ONE = 0x0001

        private const val SUBTYPE_BOOLEAN_FALSE = 0x0000
        private const val SUBTYPE_BOOLEAN_TRUE = 0x0001

        private const val SUBTYPE_URL_ABSOLUTE = 0x0001
        private const val SUBTYPE_URL_RELATIVE = 0x0002

        // Subtypes for TYPE_NUMBER seem to match CFNumberType values
        // (SUBTYPE_NUMBER_SINT8 == kCFNumberSInt8Type, etc.)
        // But not all values seem to appear in this format.
        private const val SUBTYPE_NUMBER_SINT8 = 1
        private const val SUBTYPE_NUMBER_SINT16 = 2
        private const val SUBTYPE_NUMBER_SINT32 = 3
        private const val SUBTYPE_NUMBER_SINT64 = 4
        private const val SUBTYPE_NUMBER_FLOAT32 = 5
        private const val SUBTYPE_NUMBER_FLOAT64 = 6

        /**
         * Reads a macOS bookmark from the provided blob.
         *
         * @param blob the blob to read from.
         * @return the bookmark.
         */
        fun readFrom(blob: Blob): Bookmark {
            return readFrom(blob.toBlock())
        }

        /**
         * Reads a macOS bookmark from the provided data stream.
         *
         * @param stream the stream to read from.
         * @return the bookmark.
         */
        fun readFrom(stream: DataInput): Bookmark {
            // XXX: Original code checked the stream length here. We currently abstract
            //      this way so streams don't know how long they are.
            // check data length >= 16
            // check header size in header < size in header
            // check size in header = stream size
            val header = BookmarkHeader.readFrom(stream)

            stream.position(header.headerSize)
            var tocOffset = stream.readIntLE()
            val tocs = mutableMapOf<Int, Map<TocKey, Any?>>()

            while (tocOffset != 0) {
                val tocBase = header.headerSize + tocOffset
                require(tocOffset <= header.size - header.headerSize && header.size - tocBase >= 20) {
                    "TOC offset out of range"
                }

                stream.position(tocBase)
                val tocHeader = BookmarkTocHeader.readFrom(stream)
                if (tocHeader.magic != BookmarkTocHeader.TOC_MAGIC) {
                    break
                }

                // XXX: Unexplained magic numbers in this area
                val tocSize = tocHeader.size + 8
                require(header.size - tocBase >= tocSize) { "TOC truncated" }
                require(tocSize >= BookmarkTocEntry.SIZE * tocHeader.entryCount) { "TOC entries overrun TOC size" }

                val toc = mutableMapOf<TocKey, Any?>()
                for (n in 0 until tocHeader.entryCount) {
                    val entryBase = tocBase + 20 + BookmarkTocEntry.SIZE * n
                    stream.position(entryBase)
                    val (encodedKey, valueOffset, _) = BookmarkTocEntry.readFrom(stream)
                    val key = if (encodedKey and 0x80000000.toInt() != 0) {
                        TocKey.OfString(readValue(stream, header.headerSize, encodedKey and 0x7FFFFFFF) as String)
                    } else {
                        TocKey.OfInt(encodedKey)
                    }
                    toc[key] = readValue(stream, header.headerSize, valueOffset)
                }

                tocs[tocHeader.tocId] = toc

                tocOffset = tocHeader.nextTocOffset
            }

            return Bookmark(tocs.toMap())
        }

        private fun readValue(stream: DataInput, headerSize: Int, offsetIn: Int): Any? {
            val offset = offsetIn + headerSize
            // XXX: Another place where having the length of the stream might be nice
//            if (offset > len(data) - 8)
//                raise ValueError("Offset out of range")

            stream.position(offset)
            val length = stream.readIntLE()
            val typeCode = stream.readIntLE()

//            if len(data) - offset < 8 + length:
//                raise ValueError("Data item truncated")

//            databytes = data[offset + 8 : offset + 8 + length]

            val dataSubType = typeCode and DATA_SUBTYPE_MASK
            when (typeCode and DATA_TYPE_MASK) {
                TYPE_STRING -> return stream.readString(length, StandardCharsets.UTF_8)
                TYPE_DATA -> return stream.readBlob(length)
                TYPE_NUMBER -> when (dataSubType) {
                    SUBTYPE_NUMBER_SINT8 -> return stream.readByte()
                    SUBTYPE_NUMBER_SINT16 -> return stream.readShortLE()
                    SUBTYPE_NUMBER_SINT32 -> return stream.readIntLE()
                    SUBTYPE_NUMBER_SINT64 -> return stream.readLongLE()
                    SUBTYPE_NUMBER_FLOAT32 -> return stream.readFloatLE()
                    SUBTYPE_NUMBER_FLOAT64 -> return stream.readDoubleLE()
                }
                TYPE_DATE ->
                    // Yes, dates really are stored as *BIG-endian* doubles
                    return MacTimeUtils.decodeDoubleInstant(stream.readDouble())
                TYPE_BOOLEAN -> when (dataSubType) {
                    SUBTYPE_BOOLEAN_TRUE -> return true
                    SUBTYPE_BOOLEAN_FALSE -> return false
                }
                TYPE_UUID -> return UUID(stream.readBlob(length))
                TYPE_URL -> when (dataSubType) {
                    SUBTYPE_URL_ABSOLUTE ->
                        return URL.Absolute(stream.readString(length, StandardCharsets.UTF_8))
                    SUBTYPE_URL_RELATIVE -> {
                        val baseOffset = stream.readIntLE()
                        val relativeOffset = stream.readIntLE()
                        val base = readValue(stream, headerSize, baseOffset) as URL
                        val rel = readValue(stream, headerSize, relativeOffset) as String
                        return URL.Relative(base, rel)
                    }
                }
                TYPE_ARRAY -> return buildList {
                    (offset + 8 until offset + 8 + length step 4).forEach { off ->
                        stream.position(off)
                        val elementOffset = stream.readIntLE()
                        add(readValue(stream, headerSize, elementOffset))
                    }
                }
                TYPE_DICT -> return buildMap {
                    (offset + 8 until offset + 8 + length step 8).forEach { off ->
                        stream.position(off)
                        val keyOffset = stream.readIntLE()
                        val valueOffset = stream.readIntLE()
                        val key = readValue(stream, headerSize, keyOffset)
                        val value = readValue(stream, headerSize, valueOffset)
                        this[key] = value
                    }
                }
                TYPE_NULL -> return null
            }

            return Unrecognised(typeCode, stream.readBlob(length))
        }

        /*
    @classmethod
    def _encode_item(cls, item, offset):
        if item is True:
            result = struct.pack(b"<II", 0, BMK_BOOLEAN | BMK_BOOLEAN_ST_TRUE)
        elif item is False:
            result = struct.pack(b"<II", 0, BMK_BOOLEAN | BMK_BOOLEAN_ST_FALSE)
        elif isinstance(item, str):
            encoded = item.encode("utf-8")
            result = (
                    struct.pack(b"<II", len(encoded), BMK_STRING | BMK_ST_ONE) + encoded
            )
        elif isinstance(item, bytes):
            result = struct.pack(b"<II", len(item), BMK_STRING | BMK_ST_ONE) + item
        elif isinstance(item, Data):
            result = struct.pack(
                b"<II", len(item.bytes), BMK_DATA | BMK_ST_ONE
            ) + bytes(item.bytes)
        elif isinstance(item, bytearray):
            result = struct.pack(b"<II", len(item), BMK_DATA | BMK_ST_ONE) + bytes(item)
        elif isinstance(item, int):
            if item > -0x80000000 and item < 0x7FFFFFFF:
                result = struct.pack(b"<IIi", 4, BMK_NUMBER | kCFNumberSInt32Type, item)
            else:
                result = struct.pack(b"<IIq", 8, BMK_NUMBER | kCFNumberSInt64Type, item)
        elif isinstance(item, float):
            result = struct.pack(b"<IId", 8, BMK_NUMBER | kCFNumberFloat64Type, item)
        elif isinstance(item, datetime.datetime):
            secs = item - osx_epoch
            result = struct.pack(b"<II", 8, BMK_DATE | BMK_ST_ZERO) + struct.pack(
                b">d", float(secs.total_seconds())
            )

        elif isinstance(item, uuid.UUID):
            result = struct.pack(b"<II", 16, BMK_UUID | BMK_ST_ONE) + item.bytes
        elif isinstance(item, URL):
            if item.base:
                baseoff = offset + 16
                reloff, baseenc = cls._encode_item(item.base, baseoff)
                xoffset, relenc = cls._encode_item(item.relative, reloff)
                result = b"".join(
                    [
                        struct.pack(
                            b"<IIII", 8, BMK_URL | BMK_URL_ST_RELATIVE, baseoff, reloff
                        ),
                        baseenc,
                        relenc,
                    ]
                )
            else:
                encoded = item.relative.encode("utf-8")
                result = (
                        struct.pack(b"<II", len(encoded), BMK_URL | BMK_URL_ST_ABSOLUTE)
                        + encoded
                )
        elif isinstance(item, list):
            ioffset = offset + 8 + len(item) * 4
            result = [struct.pack(b"<II", len(item) * 4, BMK_ARRAY | BMK_ST_ONE)]
            enc = []
            for elt in item:
                result.append(struct.pack(b"<I", ioffset))
                ioffset, ienc = cls._encode_item(elt, ioffset)
                enc.append(ienc)
            result = b"".join(result + enc)
        elif isinstance(item, dict):
            ioffset = offset + 8 + len(item) * 8
            result = [struct.pack(b"<II", len(item) * 8, BMK_DICT | BMK_ST_ONE)]
            enc = []
            for k, v in item.items():
                result.append(struct.pack(b"<I", ioffset))
                ioffset, ienc = cls._encode_item(k, ioffset)
                enc.append(ienc)
                result.append(struct.pack(b"<I", ioffset))
                ioffset, ienc = cls._encode_item(v, ioffset)
                enc.append(ienc)
            result = b"".join(result + enc)
        elif item is None:
            result = struct.pack(b"<II", 0, BMK_NULL | BMK_ST_ONE)
        else:
            raise ValueError("Unknown item type when encoding: %s" % item)

        offset += len(result)

        # Pad to a multiple of 4 bytes
        if offset & 3:
            extra = 4 - (offset & 3)
            result += b"\0" * extra
            offset += extra

        return (offset, result)
         */

    }
}
