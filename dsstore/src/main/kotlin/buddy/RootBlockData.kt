package buddy

import util.DataInput
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