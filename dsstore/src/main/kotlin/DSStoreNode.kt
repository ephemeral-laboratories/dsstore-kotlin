import util.Block

/**
 * Represents a single node in the tree.
 */
class DSStoreNode(
    private val nodeHeader: DSStoreNodeHeader,
    val records: List<DSStoreRecord>,
    val childNodeBlockNumbers: List<Int>,
) {
    val isExternal get() = nodeHeader.p == 0
    val isInternal get() = !isExternal
    val count get() = nodeHeader.count
    val lastChildNodeBlockNumber get() = nodeHeader.p

    companion object {

        /**
         * Reads the node from a stream.
         *
         * @param stream the stream to read from.
         * @return the read node.
         */
        fun readFrom(block: Block): DSStoreNode {
            val nodeHeader = DSStoreNodeHeader.readFrom(block)

            val records = mutableListOf<DSStoreRecord>()
            val childNodeBlockNumbers = mutableListOf<Int>()

            if (nodeHeader.p == 0) {
                // External (leaf) case
                repeat(nodeHeader.count) {
                    records.add(DSStoreRecord.readFrom(block))
                }
            } else {
                // Internal (branch) case
                repeat(nodeHeader.count) {
                    childNodeBlockNumbers.add(block.readInt())
                    records.add(DSStoreRecord.readFrom(block))
                }
            }

            return DSStoreNode(nodeHeader, records.toList(), childNodeBlockNumbers.toList())
        }
    }
}