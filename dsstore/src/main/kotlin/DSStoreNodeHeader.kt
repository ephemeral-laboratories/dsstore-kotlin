import util.DataInput

/**
 * Structure of a node header.
 *
 * @property p If 0, then this is a leaf (external) node and the header is followed by `count` records.
 *           If nonzero, then this is an internal node, and the header is followed by the block number of the leftmost
 *           child, then a record, then another block number, etc., for a total of `count` child pointers and `count`
 *           records. `p` is itself the rightmost child pointer, that is, it is logically at the end of the node.
 * @property count the number of records contained in this node.
 */
data class DSStoreNodeHeader(
    val p: Int,
    val count: Int,
) {
    companion object {

        /**
         * Reads the node header from a stream.
         *
         * @param stream the stream to read from.
         * @return the read node header.
         */
        fun readFrom(stream: DataInput): DSStoreNodeHeader {
            val p = stream.readInt()
            val count = stream.readInt()
            return DSStoreNodeHeader(p, count)
        }
    }
}
