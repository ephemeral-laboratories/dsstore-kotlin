package garden.ephemeral.macfiles.alias

object FileSystemTypes {
    const val ALIAS_FILESYSTEM_UDF = "UDF (CD/DVD)"
    const val ALIAS_FILESYSTEM_FAT32 = "FAT32"
    const val ALIAS_FILESYSTEM_EXFAT = "exFAT"
    const val ALIAS_FILESYSTEM_HFSX = "HFSX"
    const val ALIAS_FILESYSTEM_HFSPLUS = "HFS+"
    const val ALIAS_FILESYSTEM_FTP = "FTP"
    const val ALIAS_FILESYSTEM_NTFS = "NTFS"
    const val ALIAS_FILESYSTEM_UNKNOWN = "unknown"

    val FSTYPE_MAP = mapOf(
        // Version 2 aliases
        "HX" to ALIAS_FILESYSTEM_HFSX,
        "H+" to ALIAS_FILESYSTEM_HFSPLUS,
        // Version 3 aliases
        "BDcu" to ALIAS_FILESYSTEM_UDF,
        "BDIS" to ALIAS_FILESYSTEM_FAT32,
        "BDxF" to ALIAS_FILESYSTEM_EXFAT,
        "HX\u0000\u0000" to ALIAS_FILESYSTEM_HFSX,
        "H+\u0000\u0000" to ALIAS_FILESYSTEM_HFSPLUS,
        "KG\u0000\u0000" to ALIAS_FILESYSTEM_FTP,
        "NTcu" to ALIAS_FILESYSTEM_NTFS,
    )
}
