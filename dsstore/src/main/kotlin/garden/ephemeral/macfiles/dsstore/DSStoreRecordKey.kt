package garden.ephemeral.macfiles.dsstore
import garden.ephemeral.macfiles.dsstore.types.FourCC

/**
 * The parts of a [DSStoreRecord] which make up the unique key.
 *
 * Used when looking up records.
 *
 * @property filename the filename this record is storing information for.
 * @property propertyId the property ID indicating what is being stored.
 */
data class DSStoreRecordKey(
    val filename: String,
    val propertyId: FourCC,
)
