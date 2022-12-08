package garden.ephemeral.macfiles.dsstore.buddy

import garden.ephemeral.macfiles.common.io.DataInput
import garden.ephemeral.macfiles.common.io.DataOutput
import java.nio.charset.StandardCharsets

/**
 * Structure containing additional allocator state, stored in the bookkeeping block.
 *
 * @property blockAddresses allocated block addresses, with unassigned block numbers represented by zeroes.
 * @property tocEntries TOC names in ASCII, mapped to the block number for that entry.
 * @property freeLists 32 free-lists, one for each power of 2 from 2^0 to 2^31. Each free-list contains
 *           a list of offsets.
 */
data class RootBlockData(
    val blockAddresses: List<BlockAddress?>,
    val tocEntries: Map<String, Int>,
    val freeLists: List<List<Int>>,
) {
    /**
     * Calculates the space required to store this structure.
     *
     * @return the size of the data, in bytes.
     */
    fun calculateSize(): Int {
        // Starting with the int for the number of block addresses and the 4 skipped bytes
        var size = 4 + 4

        // One int for each block address
        size += blockAddresses.size * 4

        // Pad to 256-entry (1024-byte) boundary
        val extra = blockAddresses.size % 256
        if (extra != 0) {
            size += (256 - extra) * 4
        }

        // number of toc entries
        size += 4
        tocEntries.forEach { tocEntry ->
            // One byte for length of string, plus string itself, plus int
            size += 1 + tocEntry.key.length + 4
        }

        freeLists.forEach { freeList ->
            // 4 bytes for the size of the free list, followed by the int entries
            size += 4 + freeList.size * 4
        }

        return size
    }

    /**
     * Writes the root block data to a stream.
     *
     * @param stream the stream to write to.
     */
    fun writeTo(stream: DataOutput) {
        stream.writeInt(blockAddresses.size)

        // The 4 bytes we skipped originally
        stream.skip(4)

        blockAddresses.forEach { address ->
            stream.writeInt(address?.value ?: 0)
        }

        // Pad to 256-entry (1024-byte) boundary
        val extra = blockAddresses.size % 256
        if (extra != 0) {
            stream.skip((256 - extra) * 4)
        }

        stream.writeInt(tocEntries.size)
        tocEntries.forEach { tocEntry ->
            stream.writeByte(tocEntry.key.length.toByte())
            stream.writeString(tocEntry.key, StandardCharsets.US_ASCII)
            stream.writeInt(tocEntry.value)
        }

        freeLists.forEach { freeList ->
            stream.writeInt(freeList.size)
            freeList.forEach(stream::writeInt)
        }
    }

    companion object {

        /**
         * Reads the root block data from a stream.
         *
         * @param stream the stream to read from.
         * @return the read root block data.
         */
        fun readFrom(stream: DataInput): RootBlockData {
            val blockCount = stream.readInt()

            // Skipping 4 bytes which always appear to be 0
            stream.skip(4)

            val blockAddresses = buildList {
                repeat(blockCount) {
                    val addressValue = stream.readInt()
                    add(
                        if (addressValue == 0) {
                            null
                        } else {
                            BlockAddress(addressValue)
                        }
                    )
                }
            }
            // Pad to 256-entry (1024-byte) boundary
            val extra = blockCount % 256
            if (extra != 0) {
                stream.skip((256 - extra) * 4)
            }

            val tocCount = stream.readInt()
            val tocEntries = buildMap {
                repeat(tocCount) {
                    val nameLength = stream.readByte().toUByte().toInt()
                    val name = stream.readString(nameLength, StandardCharsets.US_ASCII)
                    val blockNumber = stream.readInt()
                    put(name, blockNumber)
                }
            }

            val freeLists = buildList {
                repeat(32) {
                    add(buildList {
                        val count = stream.readInt()
                        repeat(count) {
                            add(stream.readInt())
                        }
                    })
                }
            }

            return RootBlockData(blockAddresses, tocEntries, freeLists)
        }
    }
}