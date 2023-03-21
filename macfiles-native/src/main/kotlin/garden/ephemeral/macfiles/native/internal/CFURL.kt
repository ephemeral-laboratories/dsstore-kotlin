package garden.ephemeral.macfiles.native.internal

// CFURLResourcePropertyFlags enum values

const val kCFURLResourceIsRegularFile = 0x00000001L
const val kCFURLResourceIsDirectory = 0x00000002L
const val kCFURLResourceIsSymbolicLink = 0x00000004L
const val kCFURLResourceIsVolume = 0x00000008L
const val kCFURLResourceIsPackage = 0x00000010L
const val kCFURLResourceIsSystemImmutable = 0x00000020L
const val kCFURLResourceIsUserImmutable = 0x00000040L
const val kCFURLResourceIsHidden = 0x00000080L
const val kCFURLResourceHasHiddenExtension = 0x00000100L
const val kCFURLResourceIsApplication = 0x00000200L
const val kCFURLResourceIsCompressed = 0x00000400L

/* OBSOLETE -> kCFURLResourceIsCompressed */
const val kCFURLResourceIsSystemCompressed = 0x00000400L

const val kCFURLCanSetHiddenExtension = 0x00000800L
const val kCFURLResourceIsReadable = 0x00001000L
const val kCFURLResourceIsWriteable = 0x00002000L

/* execute files or search directories */
const val kCFURLResourceIsExecutable = 0x00004000L

const val kCFURLIsAliasFile = 0x00008000L
const val kCFURLIsMountTrigger = 0x00010000L

// Volume property flags

// Local device (vs. network device)
const val kCFURLVolumeIsLocal = 0x1L

// Mounted by the automounter
const val kCFURLVolumeIsAutomount = 0x2L

// Hidden from user browsing
const val kCFURLVolumeDontBrowse = 0x4L

// Mounted read-only
const val kCFURLVolumeIsReadOnly = 0x8L

// Mounted with quarantine bit
const val kCFURLVolumeIsQuarantined = 0x10L

const val kCFURLVolumeIsEjectable = 0x20L
const val kCFURLVolumeIsRemovable = 0x40L
const val kCFURLVolumeIsInternal = 0x80L
const val kCFURLVolumeIsExternal = 0x100L
const val kCFURLVolumeIsDiskImage = 0x200L
const val kCFURLVolumeIsFileVault = 0x400L
const val kCFURLVolumeIsLocaliDiskMirror = 0x800L
const val kCFURLVolumeIsiPod = 0x1000L
const val kCFURLVolumeIsiDisk = 0x2000L
const val kCFURLVolumeIsCD = 0x4000L
const val kCFURLVolumeIsDVD = 0x8000L
const val kCFURLVolumeIsDeviceFileSystem = 0x10000L
const val kCFURLVolumeSupportsPersistentIDs = 0x100000000L
const val kCFURLVolumeSupportsSearchFS = 0x200000000L
const val kCFURLVolumeSupportsExchange = 0x400000000L

// reserved						 0x800000000L
const val kCFURLVolumeSupportsSymbolicLinks = 0x1000000000L
const val kCFURLVolumeSupportsDenyModes = 0x2000000000L
const val kCFURLVolumeSupportsCopyFile = 0x4000000000L
const val kCFURLVolumeSupportsReadDirAttr = 0x8000000000L
const val kCFURLVolumeSupportsJournaling = 0x10000000000L
const val kCFURLVolumeSupportsRename = 0x20000000000L
const val kCFURLVolumeSupportsFastStatFS = 0x40000000000L
const val kCFURLVolumeSupportsCaseSensitiveNames = 0x80000000000L
const val kCFURLVolumeSupportsCasePreservedNames = 0x100000000000L
const val kCFURLVolumeSupportsFLock = 0x200000000000L
const val kCFURLVolumeHasNoRootDirectoryTimes = 0x400000000000L
const val kCFURLVolumeSupportsExtendedSecurity = 0x800000000000L
const val kCFURLVolumeSupports2TBFileSize = 0x1000000000000L
const val kCFURLVolumeSupportsHardLinks = 0x2000000000000L
const val kCFURLVolumeSupportsMandatoryByteRangeLocks = 0x4000000000000L
const val kCFURLVolumeSupportsPathFromID = 0x8000000000000L

// reserved					    0x10000000000000L,
const val kCFURLVolumeIsJournaling = 0x20000000000000L
const val kCFURLVolumeSupportsSparseFiles = 0x40000000000000L
const val kCFURLVolumeSupportsZeroRuns = 0x80000000000000L
const val kCFURLVolumeSupportsVolumeSizes = 0x100000000000000L
const val kCFURLVolumeSupportsRemoteEvents = 0x200000000000000L
const val kCFURLVolumeSupportsHiddenFiles = 0x400000000000000L
const val kCFURLVolumeSupportsDecmpFSCompression = 0x800000000000000L
const val kCFURLVolumeHas64BitObjectIDs = 0x1000000000000000L
const val kCFURLVolumePropertyFlagsAll = -1 // 0xffffffffffffffffL
