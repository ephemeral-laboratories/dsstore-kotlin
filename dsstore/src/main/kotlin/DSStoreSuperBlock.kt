import util.DataInput
import util.DataOutput

/**
 * Data structure stored in the header block.
 *
 * @property rootBlockNumber the block number of the root node of the B-tree.
 * @property levelCount the number of levels of internal nodes (tree height minus one --- that is, for a tree
 *           containing only a single, leaf, node this will be zero.)
 * @property recordCount the number of records in the tree.
 * @property nodeCount the number of nodes in the tree (tree nodes, not including this header block.)
 * @property pageSize always 0x1000, almost certainly the tree node page size.
 */
data class DSStoreSuperBlock(
    val rootBlockNumber: Int,
    val levelCount: Int,
    val recordCount: Int,
    val nodeCount: Int,
    val pageSize: Int,
) {
    /**
     * Writes the super block to a stream.
     *
     * @param stream the stream to write to.
     */
    fun writeTo(stream: DataOutput) {
        stream.writeInt(rootBlockNumber)
        stream.writeInt(levelCount)
        stream.writeInt(recordCount)
        stream.writeInt(nodeCount)
        stream.writeInt(pageSize)
    }

    companion object {
        const val SIZE = 20

        /**
         * Reads the header block from a stream.
         *
         * @param stream the stream to read from.
         * @return the read header block.
         */
        fun readFrom(stream: DataInput): DSStoreSuperBlock {
            val rootBlockNumber = stream.readInt()
            val levelCount = stream.readInt()
            val recordCount = stream.readInt()
            val nodeCount = stream.readInt()
            val pageSize = stream.readInt()
            return DSStoreSuperBlock(rootBlockNumber, levelCount, recordCount, nodeCount, pageSize)
        }
    }
}