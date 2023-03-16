package garden.ephemeral.macfiles.native.internal

import com.sun.jna.Pointer
import com.sun.jna.Structure

// Definitions found in `Finder.h`

/**
 * ```c
 * struct FileInfo {
 *     OSType              fileType;               /* The type of the file */
 *     OSType              fileCreator;            /* The file's creator */
 *     UInt16              finderFlags;            /* ex: kHasBundle, kIsInvisible... */
 *     Point               location;               /* File's location in the folder */
 *     /* If set to {0, 0}, the Finder will place the item automatically */
 *     UInt16              reservedField;          /* (set to 0) */
 * };
 * typedef struct FileInfo                 FileInfo;
 * ```
 */
@Structure.FieldOrder("fileType", "fileCreator", "finderFlags", "location", "reservedField")
internal class FileInfo(p: Pointer? = null) : Structure(p) {
    @JvmField
    var fileType: OSType = OSType(0)

    @JvmField
    var fileCreator: OSType = OSType(0)

    @JvmField
    var finderFlags: Short = 0

    @JvmField
    var location: Point = Point()

    @JvmField
    var reservedField: Short = 0

    init {
        if (p != null) {
            read()
        }
    }

    companion object {
        const val SIZE = OSType.SIZE + OSType.SIZE + 2 + Point.SIZE + 2
    }
}

/**
 * ```c
 * struct FolderInfo {
 *     Rect                windowBounds;           /* The position and dimension of the folder's window */
 *     UInt16              finderFlags;            /* ex. kIsInvisible, kNameLocked, etc.*/
 *     Point               location;               /* Folder's location in the parent folder */
 *     /* If set to {0, 0}, the Finder will place the item automatically */
 *     UInt16              reservedField;          /* (set to 0) */
 * };
 * typedef struct FolderInfo               FolderInfo;
 * ```
 */
@Structure.FieldOrder("windowBounds", "finderFlags", "location", "reservedField")
internal class FolderInfo(p: Pointer? = null) : Structure(p) {
    @JvmField
    var windowBounds: Rect = Rect()

    @JvmField
    var finderFlags: Short = 0

    @JvmField
    var location: Point = Point()

    @JvmField
    var reservedField: Short = 0

    init {
        if (p != null) {
            read()
        }
    }

    companion object {
        const val SIZE = Rect.SIZE + 2 + Point.SIZE + 2
    }
}

/**
 * ```c
 * struct ExtendedFileInfo {
 *     SInt16              reserved1[4];           /* Reserved (set to 0) */
 *     UInt16              extendedFinderFlags;    /* Extended flags (custom badge, routing info...) */
 *     SInt16              reserved2;              /* Reserved (set to 0). Comment ID if high-bit is clear */
 *     SInt32              putAwayFolderID;        /* Put away folder ID */
 * };
 * typedef struct ExtendedFileInfo         ExtendedFileInfo;
 * ```
 */
@Structure.FieldOrder("reserved1", "extendedFinderFlags", "reserved2", "putAwayFolderID")
class ExtendedFileInfo(p: Pointer? = null) : Structure(p) {
    @JvmField
    var reserved1 = ShortArray(4)

    @JvmField
    var extendedFinderFlags: Short = 0

    @JvmField
    var reserved2: Short = 0

    @JvmField
    var putAwayFolderID: Int = 0

    init {
        if (p != null) {
            read()
        }
    }

    companion object {
        const val SIZE = 2 * 4 + 2 + 2 + 4
    }
}

/**
 * ```c
 * struct ExtendedFolderInfo {
 *     Point               scrollPosition;         /* Scroll position (for icon views) */
 *     SInt32              reserved1;              /* Reserved (set to 0) */
 *     UInt16              extendedFinderFlags;    /* Extended flags (custom badge, routing info...) */
 *     SInt16              reserved2;              /* Reserved (set to 0). Comment ID if high-bit is clear */
 *     SInt32              putAwayFolderID;        /* Put away folder ID */
 * };
 * typedef struct ExtendedFolderInfo       ExtendedFolderInfo;
 * ```
 */
class ExtendedFolderInfo(p: Pointer? = null) : Structure(p) {
    @JvmField
    var scrollPosition: Point = Point()

    @JvmField
    var reserved1: Int = 0

    @JvmField
    var extendedFinderFlags: Short = 0

    @JvmField
    var reserved2: Short = 0

    @JvmField
    var putAwayFolderID: Int = 0

    init {
        if (p != null) {
            read()
        }
    }

    companion object {
        const val SIZE = Point.SIZE + 4 + 2 + 2 + 4
    }
}
