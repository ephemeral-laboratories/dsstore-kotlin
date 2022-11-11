import util.DataInput

/**
 * Data structure stored in the header block.
 *
 * @property rootBlockNumber the block number of the root node of the B-tree.
 * @property levelCount the number of levels of internal nodes (tree height minus one --- that is, for a tree
 *           containing only a single, leaf, node this will be zero.)
 * @property recordCount the number of records in the tree.
 * @property nodeCount the number of nodes in the tree (tree nodes, not including this header block.)
 * @property maybePageSize always 0x1000, probably the tree node page size.
 */
data class DSStoreHeaderBlock(
    val rootBlockNumber: Int,
    val levelCount: Int,
    val recordCount: Int,
    val nodeCount: Int,
    val maybePageSize: Int,
) {
    companion object {

        /**
         * Reads the header block from a stream.
         *
         * @param stream the stream to read from.
         * @return the read header block.
         */
        fun readFrom(stream: DataInput): DSStoreHeaderBlock {
            val rootBlockNumber = stream.readInt()
            val levelCount = stream.readInt()
            val recordCount = stream.readInt()
            val nodeCount = stream.readInt()
            val maybePageSize = stream.readInt()
            return DSStoreHeaderBlock(rootBlockNumber, levelCount, recordCount, nodeCount, maybePageSize)
        }
    }
}