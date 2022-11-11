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
     * Support method for indexing by filename to get a partial lookup
     * which can then be used to look up properties for a single file.
     *
     * @param filename the filename.
     * @return the partial lookup object.
     */
    operator fun get(filename: String): Partial {
        return Partial(filename)
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
            println("walk($blockNumber) start")
            val block = buddyFile.readBlock(blockNumber)
            val nodeHeader = DSStoreNodeHeader.readFrom(block)
            if (nodeHeader.p == 0) {
                // Node is "external" and contains just records
                repeat(nodeHeader.count) {
                    val record = DSStoreRecord.readFrom(block)
                    yield(record)
                }
            } else {
                // Node is internal and contains a mix of records and child nodes
                repeat(nodeHeader.count) {
                    val childNodeBlockNumber = block.readInt()
                    yieldAll(walk(childNodeBlockNumber))
                    val record = DSStoreRecord.readFrom(block)
                    yield(record)
                }
                yieldAll(walk(nodeHeader.p))
            }
            println("walk($blockNumber) stop")
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
        val nodeHeader = DSStoreNodeHeader.readFrom(block)
        if (nodeHeader.p == 0) {
            // Node is "external" and contains just records
            repeat(nodeHeader.count) {
                val record = DSStoreRecord.readFrom(block)
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
        } else {
            // Node is internal and contains a mix of records and child nodes
            repeat(nodeHeader.count) {
                val childNodeBlockNumber = block.readInt()
                val record = DSStoreRecord.readFrom(block)
                val comp = record.compareToKey(key)
                if (comp == 0) {
                    // record == key
                    return record
                } else if (comp < 0) {
                    // record < key, keep looking, no need to search the child node either
                } else {
                    // comp > 0, record > key, search the child block and then stop looking
                    return find(key, childNodeBlockNumber)
                }
            }
            // If we're still going by this point we have to search the right-most node still
            return find(key, nodeHeader.p)
        }
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

    /**
     * Class used for intermediate lookups. See [DSStore.get].
     */
    inner class Partial(private val filename: String) {

        /**
         * Looks up a property for a file.
         *
         * @param propertyId the property ID.
         * @return if a record is found, the decoded value, otherwise `null`.
         */
        operator fun get(propertyId: FourCC): Any? {
            val record = find(DSStoreRecordKey(filename, propertyId))
            return record?.decodeValue()
        }
    }
}