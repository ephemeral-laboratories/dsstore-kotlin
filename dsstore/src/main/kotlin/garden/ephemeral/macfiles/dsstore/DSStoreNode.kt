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
    abstract fun split(): Triple<DSStoreNode, DSStoreRecord, DSStoreNode>

    companion object {

        /**
         * Reads the node from a stream.
         *
         * @param stream the stream to read from.
         * @return the read node.
         */
        fun readFrom(stream: DataInput): DSStoreNode {
            val lastChildBlockNumber = stream.readInt()
            val count = stream.readInt()

            if (lastChildBlockNumber == 0) {
                // External (leaf) case
                val records = mutableListOf<DSStoreRecord>()
                repeat(count) {
                    records.add(DSStoreRecord.readFrom(stream))
                }
                return Leaf(records.toList())
            } else {
                // Internal (branch) case
                val records = mutableListOf<DSStoreRecord>()
                val childBlockNumbers = mutableListOf<Int>()
                repeat(count) {
                    childBlockNumbers.add(stream.readInt())
                    records.add(DSStoreRecord.readFrom(stream))
                }
                childBlockNumbers.add(lastChildBlockNumber)
                return Branch(records.toList(), childBlockNumbers.toList())
            }
        }

        private fun findPivot(records: List<DSStoreRecord>): IndexedValue<DSStoreRecord> {
            // Finding a suitable pivot index
            val totalSize = records.sumOf(DSStoreRecord::calculateSize)
            val targetSize = totalSize / 2
            var accumulatedSize = 0
            // We know this always has to find some value, because the last record
            // in the collection must bring the accumulated size to the total.
            return records.withIndex().find { (_, record) ->
                val recordSize = record.calculateSize()
                accumulatedSize += recordSize
                accumulatedSize > targetSize
            }!!
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
         * Clones the node with the record at the given index deleted.
         *
         * @param index the index to delete at.
         * @return the new node.
         */
        fun withRecordDeletedAt(index: Int): Leaf {
            val recordsCopy = records.toMutableList()
            recordsCopy.removeAt(index)
            return copy(records = recordsCopy.toList())
        }

        override fun split(): Triple<DSStoreNode, DSStoreRecord, DSStoreNode> {
            val (pivotIndex, pivot) = findPivot(records)

            return Triple(
                Leaf(records.slice(0 until pivotIndex)),
                pivot,
                Leaf(records.slice(pivotIndex + 1 until records.size))
            )
        }
    }

    data class Branch(val records: List<DSStoreRecord>, val childBlockNumbers: List<Int>) : DSStoreNode() {
        init {
            require(records.size == childBlockNumbers.size - 1) {
                "childBlockNumbers size (${childBlockNumbers.size}) " +
                        "must be one greater than records size (${records.size})"
            }
        }

        override fun calculateSize(): Int {
            return 8 + records.sumOf(DSStoreRecord::calculateSize) + childBlockNumbers.size * 4
        }

        override fun writeTo(stream: DataOutput) {
            stream.writeInt(childBlockNumbers.last())
            stream.writeInt(records.size)
            childBlockNumbers.zip(records).forEach { (childBlockNumber, record) ->
                stream.writeInt(childBlockNumber)
                record.writeTo(stream)
            }
        }

        fun withRecordReplacedAt(index: Int, newRecord: DSStoreRecord): Branch {
            val recordsCopy = records.toMutableList()
            recordsCopy[index] = newRecord
            return copy(records = recordsCopy.toList())
        }

        fun withChildBlockNumberReplacedAt(index: Int, newChildBlockNumber: Int): Branch {
            val newChildBlockNumbers = childBlockNumbers.toMutableList()
            newChildBlockNumbers[index] = newChildBlockNumber
            return copy(childBlockNumbers = newChildBlockNumbers)
        }

        fun withRecordAndFollowingChildDeletedAt(index: Int): Branch {
            val newRecords = records.toMutableList()
            val newChildBlockNumbers = childBlockNumbers.toMutableList()
            newRecords.removeAt(index)
            newChildBlockNumbers.removeAt(index + 1)
            return copy(records = newRecords, childBlockNumbers = newChildBlockNumbers)
        }

        fun withRecordAndFollowingChildReplacedAt(
            index: Int,
            newRecord: DSStoreRecord,
            newChildBlockNumber: Int
        ): DSStoreNode {
            val newRecords = records.toMutableList()
            val newChildBlockNumbers = childBlockNumbers.toMutableList()
            newRecords[index] = newRecord
            newChildBlockNumbers[index + 1] = newChildBlockNumber
            return copy(records = newRecords, childBlockNumbers = newChildBlockNumbers)
        }

        override fun split(): Triple<DSStoreNode, DSStoreRecord, DSStoreNode> {
            val (pivotIndex, pivot) = findPivot(records)

            return Triple(
                Branch(
                    records.slice(0 until pivotIndex),
                    childBlockNumbers.slice(0..pivotIndex)
                ),
                pivot,
                Branch(
                    records.slice(pivotIndex + 1 until records.size),
                    childBlockNumbers.slice((pivotIndex + 1)..records.size)
                )
            )
        }
    }
}