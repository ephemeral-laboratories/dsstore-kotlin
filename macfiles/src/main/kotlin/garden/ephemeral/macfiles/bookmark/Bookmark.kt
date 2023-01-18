package garden.ephemeral.macfiles.bookmark

import garden.ephemeral.macfiles.bookmark.types.URL
import garden.ephemeral.macfiles.bookmark.types.UUID
import garden.ephemeral.macfiles.bookmark.types.Unrecognised
import garden.ephemeral.macfiles.common.MacTimeUtils
import garden.ephemeral.macfiles.common.io.Block
import garden.ephemeral.macfiles.common.io.DataInput
import garden.ephemeral.macfiles.common.io.DataOutput
import garden.ephemeral.macfiles.common.types.Blob
import java.nio.charset.StandardCharsets
import java.time.Instant

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

    /**
     * Converts this bookmark to a blob.
     *
     * @return the blob.
     */
    fun toBlob(): Blob {
        val encodedBlobs = toBlobs()

        // This currently works by encoding the blobs up-front because we need to know the size up-front.
        // Coming up with an alternative way which doesn't need to know might speed up writing.
        return Block.create(encodedBlobs.sumOf(Blob::size)) { stream ->
            encodedBlobs.forEach(stream::writeBlob)
        }.toBlob()
    }

    /**
     * Writes this bookmark to a data stream.
     *
     * @param stream the stream to write to.
     */
    fun writeTo(stream: DataOutput) {
        val encodedBlobs = toBlobs()

        // And then everything goes to the output.
        // A future exercise would be trying to do this without all the intermediate blobs.
        encodedBlobs.forEach(stream::writeBlob)
    }

    private fun toBlobs(): List<Blob> {
        val result = mutableListOf<Blob>()
        val encodedTocs = mutableMapOf<Int, Blob>()
        // Tracking this for offset to first TOC.
        var offset = 4

        // Encode each TOC to a blob
        tocs.forEach { (tocId, toc) ->
            val entries = mutableListOf<Pair<Int, Int>>()

            toc.forEach { (tocKey, value) ->
                when (tocKey) {
                    is TocKey.OfString -> {
                        val startOffset = offset
                        val (newOffset1, enc1) = encodeItem(tocKey.value, offset)
                        result.add(enc1)
                        val (newOffset2, enc2) = encodeItem(value, newOffset1)
                        result.add(enc2)
                        entries.add(Pair(startOffset or 0x80000000.toInt(), newOffset1))
                        offset = newOffset2
                    }

                    is TocKey.OfInt -> {
                        entries.add(Pair(tocKey.value, offset))
                        val (newOffset, enc) = encodeItem(value, offset)
                        result.add(enc)
                        offset = newOffset
                    }
                }
            }

            // TOC entries must be sorted - macOS uses binary search to find data
            entries.sortBy(Pair<Int, Int>::first)

            encodedTocs[tocId] = Block.create(12 * entries.size) { stream ->
                entries.forEach { (keyOffset, valueOffset) ->
                    stream.writeIntLE(keyOffset)
                    stream.writeIntLE(valueOffset)
                    stream.writeIntLE(0)
                }
            }.toBlob()
        }

        val firstTocOffset = offset

        // Generate headers for the TOC blobs
        encodedTocs.asSequence().forEachIndexed { ndx, (tid, data) ->
            val nextOffset = if (ndx == tocs.size - 1) {
                0
            } else {
                offset + 20 + data.size
            }

            result.add(
                Block.create(20) { stream ->
                    stream.writeIntLE(data.size - 8)
                    stream.writeIntLE(0xFFFFFFFE.toInt())
                    stream.writeIntLE(tid)
                    stream.writeIntLE(nextOffset)
                    stream.writeIntLE(data.size / 12)
                }.toBlob()
            )
            result.add(data)

            offset += 20 + data.size
        }

        // Finally add the header (and the first TOC offset, which isn't part
        // of the header, but goes just after it)
        val header = Block.create(52) { stream ->
            BookmarkHeader(BookmarkHeader.MAGIC1, offset + 48, 0x10040000, 48).writeTo(stream)
            // BookmarkHeader takes care of the first 16 bytes,
            // so we pad the rest of the declared header size (48 - 16 = 32)
            repeat(4) {
                stream.writeLong(0)
            }
            stream.writeIntLE(firstTocOffset)
        }.toBlob()
        // Header of course goes at the front of the list
        result.add(0, header)
        return result
    }

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
            // require(offset <= len(data) - 8)

            stream.position(offset)
            val length = stream.readIntLE()
            val typeCode = stream.readIntLE()

            // require(data.size - offset >= 8 + length)

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

        /**
         * Builds a new immutable bookmark.
         *
         * @param action a block which is passed the builder to add content.
         * @return the built bookmark.
         */
        fun build(action: Builder.() -> Unit) = Builder().apply(action).build()

        private fun encodeItem(item: Any?, offset: Int): Pair<Int, Blob> {

            // Pad to a multiple of 4 bytes
            fun createPadded(size: Int, action: (DataOutput) -> Unit): Blob {
                var paddedSize = size
                if (size % 4 != 0) {
                    paddedSize += 4 - (size % 4)
                }
                return Block.create(paddedSize, action).toBlob()
            }

            val encoded = when (item) {
                true -> createPadded(8) { stream ->
                    stream.writeIntLE(0)
                    stream.writeIntLE(TYPE_BOOLEAN or SUBTYPE_BOOLEAN_TRUE)
                }

                false -> createPadded(8) { stream ->
                    stream.writeIntLE(0)
                    stream.writeIntLE(TYPE_BOOLEAN or SUBTYPE_BOOLEAN_FALSE)
                }

                null -> createPadded(8) { stream ->
                    stream.writeIntLE(0)
                    stream.writeIntLE(TYPE_NULL or SUBTYPE_ONE)
                }

                is String -> {
                    val encoded = item.toByteArray()
                    createPadded(8 + encoded.size) { stream ->
                        stream.writeIntLE(encoded.size)
                        stream.writeIntLE(TYPE_STRING or SUBTYPE_ONE)
                        stream.writeBlob(Blob(encoded))
                    }
                }

                is Blob -> createPadded(8 + item.size) { stream ->
                    stream.writeIntLE(item.size)
                    stream.writeIntLE(TYPE_DATA or SUBTYPE_ONE)
                    stream.writeBlob(item)
                }

                is Byte -> createPadded(9) { stream ->
                    stream.writeIntLE(1)
                    stream.writeIntLE(TYPE_NUMBER or SUBTYPE_NUMBER_SINT8)
                    stream.writeByte(item)
                }

                is Short -> createPadded(10) { stream ->
                    stream.writeIntLE(2)
                    stream.writeIntLE(TYPE_NUMBER or SUBTYPE_NUMBER_SINT16)
                    stream.writeShortLE(item)
                }

                is Int -> createPadded(12) { stream ->
                    stream.writeIntLE(4)
                    stream.writeIntLE(TYPE_NUMBER or SUBTYPE_NUMBER_SINT32)
                    stream.writeIntLE(item)
                }

                is Long -> createPadded(16) { stream ->
                    stream.writeIntLE(8)
                    stream.writeIntLE(TYPE_NUMBER or SUBTYPE_NUMBER_SINT64)
                    stream.writeLongLE(item)
                }

                is Float -> createPadded(12) { stream ->
                    stream.writeIntLE(4)
                    stream.writeIntLE(TYPE_NUMBER or SUBTYPE_NUMBER_FLOAT32)
                    stream.writeFloatLE(item)
                }

                is Double -> createPadded(16) { stream ->
                    stream.writeIntLE(8)
                    stream.writeIntLE(TYPE_NUMBER or SUBTYPE_NUMBER_FLOAT64)
                    stream.writeDoubleLE(item)
                }

                is Instant -> createPadded(16) { stream ->
                    stream.writeIntLE(8)
                    stream.writeIntLE(TYPE_DATE or SUBTYPE_ZERO)
                    // Yes, dates really are stored as *BIG-endian* doubles
                    stream.writeDouble(MacTimeUtils.encodeDoubleInstant(item))
                }

                is UUID -> createPadded(24) { stream ->
                    stream.writeIntLE(16)
                    stream.writeIntLE(TYPE_UUID or SUBTYPE_ONE)
                    stream.writeBlob(item.data)
                }

                is URL -> when (item) {
                    is URL.Absolute -> {
                        val encoded = item.value.toByteArray()
                        createPadded(8 + encoded.size) { stream ->
                            stream.writeIntLE(encoded.size)
                            stream.writeIntLE(TYPE_URL or SUBTYPE_URL_ABSOLUTE)
                            stream.writeBlob(Blob(encoded))
                        }
                    }

                    is URL.Relative -> {
                        val baseOffset = offset + 16
                        val (relativeOffset, baseEncoded) = encodeItem(item.base, baseOffset)
                        val (_, relativeEncoded) = encodeItem(item.relative, relativeOffset)
                        createPadded(16 + baseEncoded.size + relativeEncoded.size) { stream ->
                            stream.writeIntLE(8)
                            stream.writeIntLE(TYPE_URL or SUBTYPE_URL_RELATIVE)
                            stream.writeIntLE(baseOffset)
                            stream.writeIntLE(relativeOffset)
                            stream.writeBlob(baseEncoded)
                            stream.writeBlob(relativeEncoded)
                        }
                    }
                }

                is List<*> -> {
                    var elementOffset = offset + 8 + item.size * 4
                    val elementOffsets = mutableListOf<Int>()
                    val encoded = mutableListOf<Blob>()
                    item.forEach { elt ->
                        elementOffsets.add(elementOffset)
                        val (newOffset, encodedElement) = encodeItem(elt, elementOffset)
                        elementOffset = newOffset
                        encoded.add(encodedElement)
                    }

                    createPadded(8 + item.size * 4 + encoded.sumOf(Blob::size)) { stream ->
                        // Header
                        stream.writeIntLE(item.size * 4)
                        stream.writeIntLE(TYPE_ARRAY or SUBTYPE_ONE)
                        elementOffsets.forEach(stream::writeIntLE)
                        // Element data
                        encoded.forEach(stream::writeBlob)
                    }
                }

                is Map<*, *> -> {
                    var elementOffset = offset + 8 + item.size * 8
                    val elementOffsets = mutableListOf<Int>()
                    val encoded = mutableListOf<Blob>()
                    item.forEach { (key, value) ->
                        elementOffsets.add(elementOffset)
                        val (newOffset1, keyEncoded) = encodeItem(key, elementOffset)
                        elementOffset = newOffset1
                        encoded.add(keyEncoded)
                        elementOffsets.add(elementOffset)
                        val (newOffset2, valueEncoded) = encodeItem(value, elementOffset)
                        elementOffset = newOffset2
                        encoded.add(valueEncoded)
                    }

                    createPadded(8 + item.size * 8 + encoded.sumOf(Blob::size)) { stream ->
                        // Header
                        stream.writeIntLE(item.size * 8)
                        stream.writeIntLE(TYPE_DICT or SUBTYPE_ONE)
                        elementOffsets.forEach(stream::writeIntLE)
                        // Element data
                        encoded.forEach(stream::writeBlob)
                    }
                }

                else -> throw IllegalArgumentException("Unsupported item for encoding: $item")
            }

            val newOffset = offset + encoded.size
            return Pair(newOffset, encoded)
        }
    }

    /**
     * Builder passed to the action in [Bookmark.build].
     */
    class Builder {
        private val toc1 = mutableMapOf<TocKey, Any?>()
        private val tocs = mutableMapOf<Int, Map<TocKey, Any?>>()

        /**
         * Sets a value into the primary TOC.
         *
         * @param key the key.
         * @param value the value.
         */
        fun put(key: TocKey, value: Any?) {
            toc1[key] = value
        }

        /**
         * Adds an extra TOC which is passed its own map builder.
         * The built TOC is automatically added to the main builder.
         *
         * @param tocId the ID to give the new TOC.
         * @param action the block of code to run.
         */
        fun extraToc(tocId: Int, action: TocBuilder.() -> Unit) {
            val tocBuilder = TocBuilder()
            action(tocBuilder)
            tocs[tocId] = tocBuilder.build()
        }

        /**
         * Builds the immutable bookmark.
         *
         * @return the bookmark.
         */
        fun build(): Bookmark {
            tocs[1] = toc1.toMap()
            return Bookmark(tocs.toMap())
        }
    }

    /**
     * Auxiliary builder used by [Bookmark.Builder.extraToc].
     */
    class TocBuilder {
        private val toc = mutableMapOf<TocKey, Any?>()

        /**
         * Sets a value into the TOC.
         *
         * @param key the key.
         * @param value the value.
         */
        fun put(key: TocKey, value: Any?) {
            toc[key] = value
        }

        internal fun build() = toc.toMap()
    }
}
