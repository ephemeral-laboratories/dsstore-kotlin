package garden.ephemeral.macfiles.dsstore

import garden.ephemeral.macfiles.common.io.DataInput
import garden.ephemeral.macfiles.common.io.DataOutput

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

    /**
     * Writes the node to a stream.
     *
     * @param stream the stream to write to.
     */
    abstract fun writeTo(stream: DataOutput)

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

    data class Leaf(val records: List<DSStoreRecord>) : DSStoreNode() {
        override fun calculateSize(): Int {
            return 8 + records.sumOf(DSStoreRecord::calculateSize)
        }

        override fun writeTo(stream: DataOutput) {
            stream.writeInt(0)
            stream.writeInt(records.size)
            records.forEach { record ->
                record.writeTo(stream)
            }
        }

        fun withRecordReplacedAt(index: Int, newRecord: DSStoreRecord): Leaf {
            val recordsCopy = records.toMutableList()
            recordsCopy[index] = newRecord
            return copy(records = recordsCopy.toList())
        }

        fun withRecordInsertedAt(index: Int, newRecord: DSStoreRecord): Leaf {
            val recordsCopy = records.toMutableList()
            recordsCopy.add(index, newRecord)
            return copy(records = recordsCopy.toList())
        }
    }

    data class Branch(val records: List<DSStoreRecord>, val childNodeBlockNumbers: List<Int>) : DSStoreNode() {
        init {
            require(records.size == childNodeBlockNumbers.size - 1) {
                "childNodeBlockNumbers size (${childNodeBlockNumbers.size}) " +
                        "must be one greater than records size (${records.size})"
            }
        }

        override fun calculateSize(): Int {
            return 8 + records.sumOf(DSStoreRecord::calculateSize) + childNodeBlockNumbers.size * 4
        }

        override fun writeTo(stream: DataOutput) {
            stream.writeInt(childNodeBlockNumbers.last())
            stream.writeInt(records.size)
            childNodeBlockNumbers.zip(records).forEach { (childNodeBlockNumber, record) ->
                stream.writeInt(childNodeBlockNumber)
                record.writeTo(stream)
            }
        }

        fun withRecordReplacedAt(index: Int, newRecord: DSStoreRecord): Branch {
            val recordsCopy = records.toMutableList()
            recordsCopy[index] = newRecord
            return copy(records = recordsCopy.toList())
        }
    }
}