package garden.ephemeral.macfiles.alias

import garden.ephemeral.macfiles.common.types.FourCC
import java.time.Instant

/**
 * Holds information about the target filesystem entry.
 *
 * @property name the filename of the target.
 * @property kind the kind of entry the alias points to.
 * @property folderCnid the CNID (Catalog Node ID) of the target's containing folder.
 * @property cnid the CNID (Catalog Node ID) of the target.
 * @property creationDate the target's creation date.
 * @property creatorCode the target's Mac creator code.
 * @property typeCode the target's Mac type code.
 * @property levelsFrom the depth of the alias? Always seems to be `-1` on macOS.
 * @property levelsTo the depth of the target? Always seems to be `-1` on macOS.
 * @property folderName the POSIX name of the target's containing folder.
 * @property cnidPath the path from the volume root as a sequence of CNIDs.
 * @property carbonPath the Carbon path of the target.
 * @property posixPath the POSIX path of the target relative to the volume root.
 *           Note that this may or may not have a leading '/' character, but it is
 *           always relative to the containing volume.
 * @property userHomePrefixLen if the path points into a user's home folder, contains
 *           the number of folders deep that we go before we get to that home folder.
 */
data class TargetInfo(
    val name: String,
    val kind: Kind,
    val folderCnid: UInt,
    val cnid: UInt,
    val creationDate: Instant,
    val creatorCode: FourCC?,
    val typeCode: FourCC?,
    val levelsFrom: Short = -1,
    val levelsTo: Short = -1,
    val folderName: String? = null,
    val cnidPath: List<UInt>? = null,
    val carbonPath: String? = null,
    val posixPath: String? = null,
    val userHomePrefixLen: Short? = null,
) {
    class Builder(
        var name: String,
        val kind: Kind,
        val folderCnid: UInt,
        val cnid: UInt,
        var creationDate: Instant,
        val creatorCode: FourCC? = null,
        val typeCode: FourCC? = null,
        var levelsFrom: Short = -1,
        var levelsTo: Short = -1,
        var folderName: String? = null,
        var cnidPath: List<UInt>? = null,
        var carbonPath: String? = null,
        var posixPath: String? = null,
        var userHomePrefixLen: Short? = null,
    ) {
        fun build() = TargetInfo(
            name, kind, folderCnid, cnid, creationDate, creatorCode, typeCode,
            levelsFrom, levelsTo, folderName, cnidPath, carbonPath, posixPath, userHomePrefixLen
        )
    }
}
