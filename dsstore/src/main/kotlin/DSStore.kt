import buddy.BuddyFile
import types.FourCC
import util.FileMode
import java.io.Closeable
import java.nio.file.Path

/**
 * Top-level class representing the `.DS_Store` file.
 */
class DSStore(private val buddyFile: BuddyFile) : Closeable {

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
        return walk(determineRootNodeBlockNumber())
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
        return find(key, determineRootNodeBlockNumber())
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
        // Cases:
        //   - Record doesn't exist
        //     - New record should be inserted in a leaf node - may go over size!
        //   - Record is in a leaf node
        //     - Record should be replaced in the leaf node - may go over size!
        //   - Record is in a branch node
        //     - Record should be replaced in the branch node - even this may go over size!
        TODO()
    }


    private fun determineRootNodeBlockNumber(): Int {
        val headerBlockNumber = buddyFile.toc["DSDB"] ?: throw IllegalStateException("DSDB entry is missing!")
        val headerBlock = DSStoreHeaderBlock.readFrom(buddyFile.readBlock(headerBlockNumber))
        return headerBlock.rootBlockNumber
    }

    override fun close() {
        buddyFile.close()
    }

    companion object {

        /**
         * Opens a `.DS_Store` file.
         *
         * @param path the path to the file.
         * @param fileMode the mode to open the file in.
         */
        fun open(path: Path, fileMode: FileMode = FileMode.READ_ONLY): DSStore {
            val buddyFile = BuddyFile.open(path, fileMode)
            return DSStore(buddyFile)
        }
    }
}