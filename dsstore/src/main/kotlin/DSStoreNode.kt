import util.DataInput

/**
 * Represents a single node in the tree.
 */
sealed class DSStoreNode {
    /**
     * Calculates the space required to store the node.
     *
     * @return the size of the node, in bytes.
     */
    abstract fun calculateSize(): Int

    companion object {

        /**
         * Reads the node from a stream.
         *
         * @param stream the stream to read from.
         * @return the read node.
         */
        fun readFrom(stream: DataInput): DSStoreNode {
            val lastChildNodeBlockNumber = stream.readInt()
            val count = stream.readInt()

            if (lastChildNodeBlockNumber == 0) {
                // External (leaf) case
                val records = mutableListOf<DSStoreRecord>()
                repeat(count) {
                    records.add(DSStoreRecord.readFrom(stream))
                }
                return Leaf(records.toList())
            } else {
                // Internal (branch) case
                val records = mutableListOf<DSStoreRecord>()
                val childNodeBlockNumbers = mutableListOf<Int>()
                repeat(count) {
                    childNodeBlockNumbers.add(stream.readInt())
                    records.add(DSStoreRecord.readFrom(stream))
                }
                childNodeBlockNumbers.add(lastChildNodeBlockNumber)
                return Branch(records.toList(), childNodeBlockNumbers.toList())
            }
        }
    }

    class Leaf(val records: List<DSStoreRecord>) : DSStoreNode() {
        override fun calculateSize(): Int {
            return 8 + records.sumOf(DSStoreRecord::calculateSize)
        }
    }

    class Branch(val records: List<DSStoreRecord>, val childNodeBlockNumbers: List<Int>) : DSStoreNode() {
        override fun calculateSize(): Int {
            return 8 + records.sumOf(DSStoreRecord::calculateSize) + childNodeBlockNumbers.size * 4
        }
    }
}