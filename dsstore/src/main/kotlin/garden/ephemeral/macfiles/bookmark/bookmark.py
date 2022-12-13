#  This file implements the Apple "bookmark" format, which is the replacement
#  for the old-fashioned alias format.  The details of this format were
#  reverse engineered; some things are still not entirely clear.
#
import datetime
import os
import struct
import sys
import uuid
from urllib.parse import urljoin

if sys.platform == "darwin":
    from . import osx

from .utils import osx_epoch

BMK_DATA_TYPE_MASK = 0xFFFFFF00
BMK_DATA_SUBTYPE_MASK = 0x000000FF

BMK_STRING = 0x0100
BMK_DATA = 0x0200
BMK_NUMBER = 0x0300
BMK_DATE = 0x0400
BMK_BOOLEAN = 0x0500
BMK_ARRAY = 0x0600
BMK_DICT = 0x0700
BMK_UUID = 0x0800
BMK_URL = 0x0900
BMK_NULL = 0x0A00

BMK_ST_ZERO = 0x0000
BMK_ST_ONE = 0x0001

BMK_BOOLEAN_ST_FALSE = 0x0000
BMK_BOOLEAN_ST_TRUE = 0x0001

# Subtypes for BMK_NUMBER are really CFNumberType values
kCFNumberSInt8Type = 1
kCFNumberSInt16Type = 2
kCFNumberSInt32Type = 3
kCFNumberSInt64Type = 4
kCFNumberFloat32Type = 5
kCFNumberFloat64Type = 6
kCFNumberCharType = 7
kCFNumberShortType = 8
kCFNumberIntType = 9
kCFNumberLongType = 10
kCFNumberLongLongType = 11
kCFNumberFloatType = 12
kCFNumberDoubleType = 13
kCFNumberCFIndexType = 14
kCFNumberNSIntegerType = 15
kCFNumberCGFloatType = 16

# Resource property flags (from CFURLPriv.h)
kCFURLResourceIsRegularFile = 0x00000001
kCFURLResourceIsDirectory = 0x00000002
kCFURLResourceIsSymbolicLink = 0x00000004
kCFURLResourceIsVolume = 0x00000008
kCFURLResourceIsPackage = 0x00000010
kCFURLResourceIsSystemImmutable = 0x00000020
kCFURLResourceIsUserImmutable = 0x00000040
kCFURLResourceIsHidden = 0x00000080
kCFURLResourceHasHiddenExtension = 0x00000100
kCFURLResourceIsApplication = 0x00000200
kCFURLResourceIsCompressed = 0x00000400
kCFURLResourceIsSystemCompressed = 0x00000400
kCFURLCanSetHiddenExtension = 0x00000800
kCFURLResourceIsReadable = 0x00001000
kCFURLResourceIsWriteable = 0x00002000
kCFURLResourceIsExecutable = 0x00004000
kCFURLIsAliasFile = 0x00008000
kCFURLIsMountTrigger = 0x00010000

# Volume property flags (from CFURLPriv.h)
kCFURLVolumeIsLocal = 0x1
kCFURLVolumeIsAutomount = 0x2
kCFURLVolumeDontBrowse = 0x4
kCFURLVolumeIsReadOnly = 0x8
kCFURLVolumeIsQuarantined = 0x10
kCFURLVolumeIsEjectable = 0x20
kCFURLVolumeIsRemovable = 0x40
kCFURLVolumeIsInternal = 0x80
kCFURLVolumeIsExternal = 0x100
kCFURLVolumeIsDiskImage = 0x200
kCFURLVolumeIsFileVault = 0x400
kCFURLVolumeIsLocaliDiskMirror = 0x800
kCFURLVolumeIsiPod = 0x1000
kCFURLVolumeIsiDisk = 0x2000
kCFURLVolumeIsCD = 0x4000
kCFURLVolumeIsDVD = 0x8000
kCFURLVolumeIsDeviceFileSystem = 0x10000
kCFURLVolumeSupportsPersistentIDs = 0x100000000
kCFURLVolumeSupportsSearchFS = 0x200000000
kCFURLVolumeSupportsExchange = 0x400000000
# reserved                                           0x800000000
kCFURLVolumeSupportsSymbolicLinks = 0x1000000000
kCFURLVolumeSupportsDenyModes = 0x2000000000
kCFURLVolumeSupportsCopyFile = 0x4000000000
kCFURLVolumeSupportsReadDirAttr = 0x8000000000
kCFURLVolumeSupportsJournaling = 0x10000000000
kCFURLVolumeSupportsRename = 0x20000000000
kCFURLVolumeSupportsFastStatFS = 0x40000000000
kCFURLVolumeSupportsCaseSensitiveNames = 0x80000000000
kCFURLVolumeSupportsCasePreservedNames = 0x100000000000
kCFURLVolumeSupportsFLock = 0x200000000000
kCFURLVolumeHasNoRootDirectoryTimes = 0x400000000000
kCFURLVolumeSupportsExtendedSecurity = 0x800000000000
kCFURLVolumeSupports2TBFileSize = 0x1000000000000
kCFURLVolumeSupportsHardLinks = 0x2000000000000
kCFURLVolumeSupportsMandatoryByteRangeLocks = 0x4000000000000
kCFURLVolumeSupportsPathFromID = 0x8000000000000
# reserved                                      0x10000000000000
kCFURLVolumeIsJournaling = 0x20000000000000
kCFURLVolumeSupportsSparseFiles = 0x40000000000000
kCFURLVolumeSupportsZeroRuns = 0x80000000000000
kCFURLVolumeSupportsVolumeSizes = 0x100000000000000
kCFURLVolumeSupportsRemoteEvents = 0x200000000000000
kCFURLVolumeSupportsHiddenFiles = 0x400000000000000
kCFURLVolumeSupportsDecmpFSCompression = 0x800000000000000
kCFURLVolumeHas64BitObjectIDs = 0x1000000000000000
kCFURLVolumePropertyFlagsAll = 0xFFFFFFFFFFFFFFFF

BMK_URL_ST_ABSOLUTE = 0x0001
BMK_URL_ST_RELATIVE = 0x0002

# Bookmark keys
kBookmarkURL = 0x1003  # A URL
kBookmarkPath = 0x1004  # Array of path components
kBookmarkCNIDPath = 0x1005  # Array of CNIDs
kBookmarkFileProperties = (
    0x1010  # (CFURL rp flags, CFURL rp flags asked for, 8 bytes NULL)
)
kBookmarkFileName = 0x1020
kBookmarkFileID = 0x1030
kBookmarkFileCreationDate = 0x1040
# = 0x1054   # ?
# = 0x1055   # ?
# = 0x1056   # ?
# = 0x1101   # ?
# = 0x1102   # ?
kBookmarkTOCPath = 0x2000  # A list of (TOC id, ?) pairs
kBookmarkVolumePath = 0x2002
kBookmarkVolumeURL = 0x2005
kBookmarkVolumeName = 0x2010
kBookmarkVolumeUUID = 0x2011  # Stored (perversely) as a string
kBookmarkVolumeSize = 0x2012
kBookmarkVolumeCreationDate = 0x2013
kBookmarkVolumeProperties = (
    0x2020  # (CFURL vp flags, CFURL vp flags asked for, 8 bytes NULL)
)
kBookmarkVolumeIsRoot = 0x2030  # True if volume is FS root
kBookmarkVolumeBookmark = 0x2040  # Embedded bookmark for disk image (TOC id)
kBookmarkVolumeMountPoint = 0x2050  # A URL
# = 0x2070
kBookmarkContainingFolder = 0xC001  # Index of containing folder in path
kBookmarkUserName = 0xC011  # User that created bookmark
kBookmarkUID = 0xC012  # UID that created bookmark
kBookmarkWasFileReference = 0xD001  # True if the URL was a file reference
kBookmarkCreationOptions = 0xD010
kBookmarkURLLengths = 0xE003  # See below
kBookmarkDisplayName = 0xF017
kBookmarkIconData = 0xF020
kBookmarkIconRef = 0xF021
kBookmarkTypeBindingData = 0xF022
kBookmarkCreationTime = 0xF030
kBookmarkSandboxRwExtension = 0xF080
kBookmarkSandboxRoExtension = 0xF081
kBookmarkAliasData = 0xFE00

# Alias for backwards compatibility
kBookmarkSecurityExtension = kBookmarkSandboxRwExtension

# kBookmarkURLLengths is an array that is set if the URL encoded by the
# bookmark had a base URL; in that case, each entry is the length of the
# base URL in question.  Thus a URL
#
#     file:///foo/bar/baz    blam/blat.html
#
# will result in [3, 2], while the URL
#
#     file:///foo    bar/baz    blam    blat.html
#
# would result in [1, 2, 1, 1]


class Data:
    def __init__(self, bytedata=None):
        #: The bytes, stored as a byte string
        self.bytes = bytes(bytedata)

    def __repr__(self):
        return "Data(%r)" % self.bytes


class URL:
    def __init__(self, base, rel=None):
        if rel is not None:
            #: The base URL, if any (a :class:`URL`)
            self.base = base
            #: The rest of the URL (a string)
            self.relative = rel
        else:
            self.base = None
            self.relative = base

    @property
    def absolute(self):
        """Return an absolute URL."""
        if self.base is None:
            return self.relative
        else:
            return urljoin(self.base.absolute, self.relative)

    def __repr__(self):
        return "URL(%r)" % self.absolute


