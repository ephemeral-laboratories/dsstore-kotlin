package garden.ephemeral.macfiles.dsstore

import garden.ephemeral.macfiles.common.io.Block
import garden.ephemeral.macfiles.common.types.FourCC
import garden.ephemeral.macfiles.dsstore.buddy.BuddyFile
import garden.ephemeral.macfiles.dsstore.util.FileMode
import java.io.Closeable
import java.nio.file.Path

/**
 * Top-level class representing the `.DS_Store` file.
 */
class DSStore(private val buddyFile: BuddyFile) : Closeable {
    private val superBlockNumber = buddyFile.getTocEntry(SUPERBLOCK_KEY)
    private var superBlock = DSStoreSuperBlock.readFrom(
        buddyFile.readBlock(superBlockNumber)
    )

    /**
     * Support method for convenient indexing.
     * Finds the record and decodes the value.
     *
     * @param filename the filename.
     * @param propertyId the property ID to get.
     * @return the decoded value.
     */
    operator fun get(filename: String, propertyId: FourCC): Any? {
        val record = find(DSStoreRecordKey(filename, propertyId))
        return record?.decodeValue()
    }

    /**
     * Support method for convenient indexing when setting.
     * Encodes the value into a record and then inserts the record.
     *
     * @param filename the filename.
     * @param propertyId the property ID to set.
     * @param value the value.
     */
    operator fun set(filename: String, propertyId: FourCC, value: Any) {
        val record = DSStoreRecord(filename, propertyId, value)
        insertOrReplace(record)
    }

    /**
     * Walks the store, returning a sequence of all keys.
     *
     * @return the sequence.
     */
    fun walk(): Sequence<DSStoreRecord> {
        return walk(superBlock.rootBlockNumber)
    }

    private fun walk(blockNumber: Int): Sequence<DSStoreRecord> {
        return sequence {
            when (val node = readNode(blockNumber)) {
                is DSStoreNode.Leaf -> yieldAll(node.records)
                is DSStoreNode.Branch -> {
                    node.childBlockNumbers.zip(node.records).forEach { (childBlockNumber, record) ->
                        yieldAll(walk(childBlockNumber))
                        yield(record)
                    }
                    yieldAll(walk(node.childBlockNumbers.last()))
                }
            }
        }
    }

    /**
     * Finds a key in the store.
     *
     * @param key the key.
     * @return the found record, or `null` if not found.
     */
    fun find(key: DSStoreRecordKey): DSStoreRecord? {
        return find(key, superBlock.rootBlockNumber)
    }

    private fun find(key: DSStoreRecordKey, blockNumber: Int): DSStoreRecord? {
        // Logic in here is VERY similar to the logic in `walk` -
        // the main difference is that it skips iterating children if the value cannot be there.
        return when (val node = readNode(blockNumber)) {
            is DSStoreNode.Leaf -> {
                when (val result = findRecord(node.records, key)) {
                    is Found -> result.record
                    is NotFound -> null
                }
            }
            is DSStoreNode.Branch -> {
                when (val result = findRecord(node.records, key)) {
                    is Found -> result.record
                    is NotFound -> find(key, node.childBlockNumbers[result.index])
                }
            }
        }
    }

    /**
     * Deletes a record.
     *
     * Does nothing if the record did not exist.
     *
     * @param filename the record filename.
     * @param propertyId the record property ID.
     */
    fun delete(filename: String, propertyId: FourCC) {
        delete(DSStoreRecordKey(filename, propertyId))
    }

    /**
     * Deletes a record.
     *
     * Does nothing if the record did not exist.
     *
     * @param key the key for looking up the record.
     */
    fun delete(key: DSStoreRecordKey) {
        val defunctBlocks = mutableListOf<Int>()
        val newRootBlockNumber = deleteInner(superBlock.rootBlockNumber, key, defunctBlocks)
        if (newRootBlockNumber != null) {
            updateSuperBlock { s -> s.copy(rootBlockNumber = newRootBlockNumber) }
            defunctBlocks.forEach(buddyFile::releaseBlock)
        }
    }

    private fun deleteInner(
        blockNumber: Int,
        key: DSStoreRecordKey,
        defunctBlocks: MutableList<Int>
    ): Int? {
        val newNode = when (val node = readNode(blockNumber)) {
            is DSStoreNode.Leaf -> {
                when (val result = findRecord(node.records, key)) {
                    is Found -> node.withRecordDeletedAt(result.index)
                    is NotFound -> null
                }
            }
            is DSStoreNode.Branch -> {
                when (val result = findRecord(node.records, key)) {
                    is Found -> {
                        // record == key
                        val nextChildBlockNumber = node.childBlockNumbers[result.index + 1]
                        val firstRecordInNextChild = findFirstRecord(nextChildBlockNumber)
                        if (firstRecordInNextChild == null) {
                            // next child is empty too, so we can just remove both!
                            defunctBlocks.add(nextChildBlockNumber)
                            node.withRecordAndFollowingChildDeletedAt(result.index)
                        } else {
                            // We know it exists now, so we can delete it from the child
                            val newChildBlockNumber =
                                deleteInner(nextChildBlockNumber, firstRecordInNextChild.extractKey(), defunctBlocks)!!
                            // Then put it into the current node
                            node.withRecordAndFollowingChildReplacedAt(
                                result.index,
                                firstRecordInNextChild,
                                newChildBlockNumber
                            )
                        }
                    }
                    is NotFound -> {
                        val newChildBlockNumber =
                            deleteInner(node.childBlockNumbers[result.index], key, defunctBlocks) ?: return null
                        node.withChildBlockNumberReplacedAt(result.index, newChildBlockNumber)
                    }
                }
            }
        } ?: return null
        return updateNode(newNode, blockNumber, defunctBlocks)
    }

    private fun findFirstRecord(blockNumber: Int): DSStoreRecord? {
        when (val node = readNode(blockNumber)) {
            is DSStoreNode.Leaf -> {
                return node.records.firstOrNull()
            }

            is DSStoreNode.Branch -> {
                val found = findFirstRecord(node.childBlockNumbers.first())
                if (found != null) {
                    return found
                }
                return node.records.first()
            }
        }
    }

    /**
     * Inserts a new record. If the record already exists, it is replaced.
     *
     * @param record the new record.
     */
    fun insertOrReplace(record: DSStoreRecord) {
        val defunctBlocks = mutableListOf<Int>()
        val key = record.extractKey()
        val newRootBlockNumber = insertOrReplaceInner(superBlock.rootBlockNumber, key, record, defunctBlocks)
        updateSuperBlock { s -> s.copy(rootBlockNumber = newRootBlockNumber) }
        defunctBlocks.forEach(buddyFile::releaseBlock)
    }

    private fun insertOrReplaceInner(
        blockNumber: Int,
        key: DSStoreRecordKey,
        newRecord: DSStoreRecord,
        defunctBlocks: MutableList<Int>
    ): Int {
        val newNode = when (val node = readNode(blockNumber)) {
            is DSStoreNode.Leaf -> {
                when (val result = findRecord(node.records, key)) {
                    is Found -> node.withRecordReplacedAt(result.index, newRecord)
                    is NotFound -> node.withRecordInsertedAt(result.index, newRecord)
                }
            }
            is DSStoreNode.Branch -> {
                when (val result = findRecord(node.records, key)) {
                    is Found -> node.withRecordReplacedAt(result.index, newRecord)
                    is NotFound -> {
                        val newChildBlockNumber =
                            insertOrReplaceInner(node.childBlockNumbers[result.index], key, newRecord, defunctBlocks)
                        // XXX: Slightly less than great thing here - if the child block was a newly-created
                        //      branch which will only have 2 children, that whole branch might fit inside the
                        //      current branch. So here, we could try to merge the two. It isn't required to
                        //      get a working file.
                        node.withChildBlockNumberReplacedAt(result.index, newChildBlockNumber)
                    }
                }
            }
        }
        return updateNode(newNode, blockNumber, defunctBlocks)
    }

    private fun updateNode(newNodeIn: DSStoreNode, existingBlockNumber: Int, defunctBlocks: MutableList<Int>): Int {
        var newNode = newNodeIn
        val newNodeSize = newNode.calculateSize()
        if (newNodeSize > superBlock.pageSize) {
            val (nodeBefore, pivot, nodeAfter) = newNode.split()
            val nodeBeforeBlockNumber = writeNode(nodeBefore)
            val nodeAfterBlockNumber = writeNode(nodeAfter)
            newNode = DSStoreNode.Branch(
                records = listOf(pivot),
                childBlockNumbers = listOf(nodeBeforeBlockNumber, nodeAfterBlockNumber)
            )
        }
        val newBlockNumber = writeNode(newNode)
        defunctBlocks.add(existingBlockNumber)
        return newBlockNumber
    }

    private fun writeNode(newNode: DSStoreNode): Int {
        val newNodeSize = newNode.calculateSize()
        require(newNodeSize <= superBlock.pageSize) {
            "Node size ($newNodeSize) exceeds page size (${superBlock.pageSize})"
        }
        val newNodeBlock = Block.create(newNodeSize) { stream -> newNode.writeTo(stream) }
        return buddyFile.allocateAndWriteBlock(newNodeBlock)
    }

    private fun readNode(blockNumber: Int): DSStoreNode {
        val block = buddyFile.readBlock(blockNumber)
        return DSStoreNode.readFrom(block)
    }

    private fun updateSuperBlock(modification: (DSStoreSuperBlock) -> DSStoreSuperBlock) {
        superBlock = modification(superBlock)
        buddyFile.writeBlock(superBlockNumber, Block.create(DSStoreSuperBlock.SIZE) { stream ->
            superBlock.writeTo(stream)
        })
    }

    override fun close() {
        buddyFile.close()
    }

    companion object {
        const val SUPERBLOCK_KEY = "DSDB"

        /**
         * Opens a `.DS_Store` file.
         *
         * @param path the path to the file.
         * @param fileMode the mode to open the file in.
         */
        fun open(path: Path, fileMode: FileMode = FileMode.READ_ONLY): DSStore {
            val buddyFile = BuddyFile.open(path, fileMode)

            if (!buddyFile.hasTocEntry(SUPERBLOCK_KEY) && fileMode == FileMode.READ_WRITE) {
                val superBlockNumber = buddyFile.allocateTocEntry(
                    SUPERBLOCK_KEY,
                    DSStoreSuperBlock.SIZE
                )
                val pageSize = 4096
                val rootBlockNumber = buddyFile.allocateBlock(pageSize)

                buddyFile.writeBlock(rootBlockNumber, Block.create(pageSize) {
                    // will be zero filled by default, so no need to do anything
                })

                buddyFile.writeBlock(superBlockNumber, Block.create(DSStoreSuperBlock.SIZE) { stream ->
                    val superBlock = DSStoreSuperBlock(
                        rootBlockNumber,
                        0,
                        0,
                        1,
                        pageSize
                    )
                    superBlock.writeTo(stream)
                })
            }

            return DSStore(buddyFile)
        }

        /**
         * Searches for a key in a list of records.
         *
         * @param records the records to search in.
         * @param key the key to search for.
         * @return a result, either [Found] (in which case includes the index and the record),
         *         or [NotFound] (in which case includes the insertion index.)
         */
        private fun findRecord(records: List<DSStoreRecord>, key: DSStoreRecordKey) : RecordSearchResult {
            records.forEachIndexed { index, record ->
                val comp = record.compareToKey(key)
                if (comp == 0) {
                    return Found(index, record)
                } else if (comp > 0) {
                    return NotFound(index)
                }
            }
            return NotFound(records.size)
        }
        private sealed class RecordSearchResult
        private data class Found(val index: Int, val record: DSStoreRecord) : RecordSearchResult()
        private data class NotFound(val index: Int) : RecordSearchResult()
    }
}