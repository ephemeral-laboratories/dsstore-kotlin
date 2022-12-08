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

        /**
         * Clones the node with one record replaced at the given index.
         *
         * @param index the index to replace.
         * @param newRecord the new record to replace it with.
         * @return the new node.
         */
        fun withRecordReplacedAt(index: Int, newRecord: DSStoreRecord): Leaf {
            val recordsCopy = records.toMutableList()
            recordsCopy[index] = newRecord
            return copy(records = recordsCopy.toList())
        }

        /**
         * Clones the node with one additional record inserted at the given index.
         *
         * @param index the index to insert at.
         * @param newRecord the new record to insert.
         * @return the new node.
         */
        fun withRecordInsertedAt(index: Int, newRecord: DSStoreRecord): Leaf {
            val recordsCopy = records.toMutableList()
            recordsCopy.add(index, newRecord)
            return copy(records = recordsCopy.toList())
        }

        /**
         * Splits the node into two nodes with a pivot record.
         * The returned nodes would end up in two new blocks, while the pivot record
         * would be stored directly in the branch node.
         *
         * @return a triple containing:
         *         - a node containing all the records before the pivot point
         *         - a record at the pivot point
         *         - a node containing all the records after the pivot point
         */
        fun split(): Triple<DSStoreNode, DSStoreRecord, DSStoreNode> {
            // Finding a suitable pivot index
            val totalSize = records.sumOf(DSStoreRecord::calculateSize)
            val targetSize = totalSize / 2
            var accumulatedSize = 0
            // We know this always has to find some value, because the last record
            // in the collection must bring the accumulated size to the total.
            val (pivotIndex, pivot) = records.withIndex().find { (_, record) ->
                val recordSize = record.calculateSize()
                accumulatedSize += recordSize
                accumulatedSize > targetSize
            }!!

            return Triple(
                Leaf(records.slice(0..pivotIndex)),
                pivot,
                Leaf(records.slice(pivotIndex + 1 until records.size))
            )
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

        fun withChildBlockNumberReplacedAt(index: Int, newChildBlockNumber: Int): Branch {
            val newChildBlockNumbers = childNodeBlockNumbers.toMutableList()
            newChildBlockNumbers[index] = newChildBlockNumber
            return copy(childNodeBlockNumbers = newChildBlockNumbers)
        }
    }
}