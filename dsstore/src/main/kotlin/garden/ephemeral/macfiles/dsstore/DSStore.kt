package garden.ephemeral.macfiles.dsstore

import garden.ephemeral.macfiles.dsstore.buddy.BuddyFile
import garden.ephemeral.macfiles.dsstore.types.FourCC
import garden.ephemeral.macfiles.dsstore.util.Block
import garden.ephemeral.macfiles.dsstore.util.FileMode
import java.io.Closeable
import java.nio.file.Path

/**
 * Top-level class representing the `.DS_Store` file.
 */
class DSStore(private val buddyFile: BuddyFile) : Closeable {
    private val superBlock: DSStoreSuperBlock

    init {
        val superBlockNumber = buddyFile.getTocEntry(SUPERBLOCK_KEY)
        superBlock = DSStoreSuperBlock.readFrom(
            buddyFile.readBlock(superBlockNumber)
        )
    }

    /**
     * Support method for convenient indexing.
     * Finds the record and decodes the value.
     *
     * @param filename the filename.
     * @return the partial lookup object.
     */
    operator fun get(filename: String, propertyId: FourCC): Any? {
        val record = find(DSStoreRecordKey(filename, propertyId))
        return record?.decodeValue()
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
            val block = buddyFile.readBlock(blockNumber)
            when (val node = DSStoreNode.readFrom(block)) {
                is DSStoreNode.Leaf -> yieldAll(node.records)
                is DSStoreNode.Branch -> {
                    node.childNodeBlockNumbers.zip(node.records).forEach { (childNodeBlockNumber, record) ->
                        yieldAll(walk(childNodeBlockNumber))
                        yield(record)
                    }
                    yieldAll(walk(node.childNodeBlockNumbers.last()))
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
        val block = buddyFile.readBlock(blockNumber)
        when (val node = DSStoreNode.readFrom(block)) {
            is DSStoreNode.Leaf -> {
                node.records.forEach { record ->
                    val comp = record.compareToKey(key)
                    if (comp == 0) {
                        // record == key
                        return record
                    } else if (comp < 0) {
                        // record < key, keep looking
                    } else {
                        // comp > 0, record > key, stop looking
                        return null
                    }
                }
                return null
            }

            is DSStoreNode.Branch -> {
                node.records.forEachIndexed { index, record ->
                    val comp = record.compareToKey(key)
                    if (comp == 0) {
                        // record == key
                        return record
                    } else if (comp < 0) {
                        // record < key, keep looking, no need to search the child node either
                    } else {
                        // comp > 0, record > key, search the previous child block and then stop looking
                        return find(key, node.childNodeBlockNumbers[index])
                    }
                }
                // If we're still going by this point we have to search the right-most node still
                return find(key, node.childNodeBlockNumbers.last())
            }
        }
    }

    /**
     * Deletes a record.
     *
     * @param key the key for looking up the record.
     */
    fun delete(key: DSStoreRecordKey) {
        // Cases:
        //   - Record doesn't exist - nothing to do
        //   - Record is in a leaf node
        //     - Record should be deleted in the leaf node - may become empty!
        //   - Record is in a branch node
        //     - Record should be deleted in the branch node - now have to peel some record off
        //       one of the leaves either side to put in as the dividing record
        TODO()
    }

    /**
     * Inserts a new record. If the record already exists, it is replaced.
     */
    fun insertOrReplace(record: DSStoreRecord) {
        insertOrReplaceInner(superBlock.rootBlockNumber, record)
    }

    // TODO: Try to figure out the best way to reuse walk logic between delete, insert, find
    private fun insertOrReplaceInner(blockNumber: Int, newRecord: DSStoreRecord): Int? {
        val key = newRecord.extractKey()
        val block = buddyFile.readBlock(blockNumber)
        when (val node = DSStoreNode.readFrom(block)) {
            is DSStoreNode.Leaf -> {
                node.records.forEachIndexed { index, record ->
                    val comp = record.compareToKey(key)
                    if (comp == 0) {
                        // record == key
                        return updateLeafNode(node, blockNumber) { it[index] = newRecord }
                    } else if (comp < 0) {
                        // record < key, keep looking
                    } else {
                        // comp > 0, record > key, stop looking, current index is the insertion point
                        return updateLeafNode(node, blockNumber) { it.add(index, newRecord) }
                    }
                }
                // insertion point is at the end
                return updateLeafNode(node, blockNumber) { it.add(newRecord) }
            }

            is DSStoreNode.Branch -> {
                node.records.forEachIndexed { index, record ->
                    val comp = record.compareToKey(key)
                    if (comp == 0) {
                        // record == key
                        TODO("What now?")
                    } else if (comp < 0) {
                        // record < key, keep looking, no need to search the child node either
                    } else {
                        // comp > 0, record > key, search the previous child block and then stop looking
                        TODO("What now?")
                    }
                }
                // If we're still going by this point we have to search the right-most node still
                TODO("What now?")
            }
        }
    }

    private fun updateLeafNode(
        node: DSStoreNode.Leaf,
        existingBlockNumber: Int,
        recordsMutator: (records: MutableList<DSStoreRecord>) -> Unit
    ): Int {
        val newRecords = node.records.toMutableList()
        recordsMutator(newRecords)
        val newNode = node.copy(records = newRecords)
        val newNodeSize = newNode.calculateSize()
        if (newNodeSize > superBlock.pageSize) {
            TODO("Node size would exceed page size, splitting nodes is not yet implemented")
        }
        val newNodeBlock = Block.create(newNodeSize) { stream -> newNode.writeTo(stream) }
        val newBlockNumber = buddyFile.allocateBlock(newNodeBlock.size, existingBlockNumber)
        buddyFile.writeBlock(newBlockNumber, newNodeBlock)
        return newBlockNumber
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
    }
}