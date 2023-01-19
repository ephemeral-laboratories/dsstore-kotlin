package garden.ephemeral.macfiles.alias

/**
 * Enumeration of known filesystem types.
 *
 * **Caveat:**
 * Historically, a 2-char identifier was used for these.
 * However, newer Alias file formats added support for 4-char identifiers.
 *
 * If you take one of the enum values with a 2-char identifier,
 * and write it to an alias file in the 4-char format, it will be padded out with nulls.
 * This appears to match what macOS itself is doing.
 *
 * If you take one of the enum values with a 4-char identifier,
 * and write it to an alias file in the 2-char format, it will simply be truncated.
 * When such an identifier is read back in, it's going to be "unknown".
 */
enum class FileSystemType(var identifier: String, var displayName: String) {
    UNKNOWN("", "unknown"),

    HFSX("HX", "HFSX"),
    HFS_PLUS("H+", "HFS+"),
    FTP("KG", "FTP"),

    UDF("BDcu", "UDF (CD/DVD)"),
    FAT32("BDIS", "FAT32"),
    EXFAT("BDxF", "exFAT"),
    NTFS("NTcu", "NTFS"),

    ;

    companion object {
        private val byIdentifier = FileSystemType.values().associateBy(FileSystemType::identifier)

        fun forIdentifier(identifier: String): FileSystemType {
            val trimmed = identifier.trimEnd('\u0000')
            return byIdentifier[trimmed] ?: UNKNOWN
        }
    }
}
