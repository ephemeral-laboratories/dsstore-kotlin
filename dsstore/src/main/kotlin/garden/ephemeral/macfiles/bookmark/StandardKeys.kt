package garden.ephemeral.macfiles.bookmark

/**
 * Repository of known bookmark keys.
 */
object StandardKeys {
    // A URL
    val kBookmarkURL = TocKey.OfInt(0x1003)

    // Array of path components
    val kBookmarkPath = TocKey.OfInt(0x1004)

    // Array of CNIDs
    val kBookmarkCNIDPath = TocKey.OfInt(0x1005)

    // (CFURL rp flags, CFURL rp flags asked for, 8 bytes NULL)
    val kBookmarkFileProperties = TocKey.OfInt(0x1010)

    val kBookmarkFileName = TocKey.OfInt(0x1020)

    val kBookmarkFileID = TocKey.OfInt(0x1030)

    val kBookmarkFileCreationDate = TocKey.OfInt(0x1040)

    // ??? = TocKey.OfInt(0x1054)   # ?
    // ??? = TocKey.OfInt(0x1055)   # ?
    // ??? = TocKey.OfInt(0x1056)   # ?
    // ??? = TocKey.OfInt(0x1101)   # ?
    // ??? = TocKey.OfInt(0x1102)   # ?

    // A list of (TOC id, ?) pairs
    val kBookmarkTOCPath = TocKey.OfInt(0x2000)

    val kBookmarkVolumePath = TocKey.OfInt(0x2002)

    val kBookmarkVolumeURL = TocKey.OfInt(0x2005)

    val kBookmarkVolumeName = TocKey.OfInt(0x2010)

    // Stored (perversely) as a string
    val kBookmarkVolumeUUID = TocKey.OfInt(0x2011)

    val kBookmarkVolumeSize = TocKey.OfInt(0x2012)

    val kBookmarkVolumeCreationDate = TocKey.OfInt(0x2013)

    // # (CFURL vp flags, CFURL vp flags asked for, 8 bytes NULL)
    val kBookmarkVolumeProperties = TocKey.OfInt(0x2020)

    // True if volume is FS root
    val kBookmarkVolumeIsRoot = TocKey.OfInt(0x2030)

    // # Embedded bookmark for disk image (TOC id)
    val kBookmarkVolumeBookmark = TocKey.OfInt(0x2040)

    // # A URL
    val kBookmarkVolumeMountPoint = TocKey.OfInt(0x2050)

    // ??? = TocKey.OfInt(0x2070)

    // Index of containing folder in path
    val kBookmarkContainingFolder = TocKey.OfInt(0xC001)

    // # User that created bookmark
    val kBookmarkUserName = TocKey.OfInt(0xC011)

    // UID that created bookmark
    val kBookmarkUID = TocKey.OfInt(0xC012)

    // True if the URL was a file reference
    val kBookmarkWasFileReference = TocKey.OfInt(0xD001)

    val kBookmarkCreationOptions = TocKey.OfInt(0xD010)

    // See below
    val kBookmarkURLLengths = TocKey.OfInt(0xE003)

    val kBookmarkDisplayName = TocKey.OfInt(0xF017)

    val kBookmarkIconData = TocKey.OfInt(0xF020)

    val kBookmarkIconRef = TocKey.OfInt(0xF021)

    val kBookmarkTypeBindingData = TocKey.OfInt(0xF022)

    val kBookmarkCreationTime = TocKey.OfInt(0xF030)

    val kBookmarkSandboxRwExtension = TocKey.OfInt(0xF080)

    val kBookmarkSandboxRoExtension = TocKey.OfInt(0xF081)

    val kBookmarkAliasData = TocKey.OfInt(0xFE00)

}