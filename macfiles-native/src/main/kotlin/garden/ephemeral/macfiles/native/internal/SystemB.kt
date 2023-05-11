package garden.ephemeral.macfiles.native.internal

import com.sun.jna.*
import java.time.*

internal interface SystemB : Library {

    /**
     * Gets statistics for the filesystem which contains the file with the given path.
     *
     * ```c
     * int
     * statfs(const char *path, struct statfs *buf);
     *
     * int
     * statfs64(const char *path, struct statfs *buf);
     * ```
     *
     * The macOS docs say that these two methods behave identically. However, we find that on
     * the latest macOS, an additional 16 bytes are appearing somewhere inside the structure.
     * Thus, for now, we call the deprecated `statfs64` variant, and hope that macOS does something
     * about fixing the other method before removing it.
     *
     * @param path the path to the file.
     * @param buf a statfs structure which will be filled during the call.
     */
    @Throws(LastErrorException::class)
    fun statfs64(path: ByteArray, buf: Statfs)

    /**
     * Gets attributes for a file, directory, volume, etc.
     *
     * ```c
     * int
     * getattrlist(
     *     const char* path,
     *     struct attrlist * attrList,
     *     void * attrBuf,
     *     size_t attrBufSize,
     *     unsigned long options
     *     );
     * ```
     *
     * @param path the path to the thing.
     * @param
     */
    @Throws(LastErrorException::class)
    fun getattrlist(path: ByteArray, attrlist: Attrlist, attrBuf: Pointer, attrBufSize: Size_t, options: Long)


    companion object {
        var INSTANCE: SystemB = Native.load("System", SystemB::class.java)

        /**
         * ```c
         * #define MFSTYPENAMELEN  16 /* length of fs type name including null */
         * ```
         */
        const val MFSTYPENAMELEN = 16

        /**
         * ```c
         * #define MAXPATHLEN      1024
         * ```
         */
        const val MAXPATHLEN = 1024

        /**
         * ```c
         * #define MNAMELEN        MAXPATHLEN
         * ```
         */
        const val MNAMELEN = MAXPATHLEN

        /**
         * #define NAME_MAX 255
         */
        const val NAME_MAX = 255

        /**
         * #define PATH_MAX 1024
         */
        const val PATH_MAX = 1024

        /**
         * ```c
         * #define ATTR_BIT_MAP_COUNT 5
         * ```
         */
        const val ATTR_BIT_MAP_COUNT = 5.toShort()

        // Common attributes
        val ATTR_CMN_NAME = Attrgroup_t(0x00000001)
        val ATTR_CMN_DEVID = Attrgroup_t(0x00000002)
        val ATTR_CMN_FSID = Attrgroup_t(0x00000004)
        val ATTR_CMN_OBJTYPE = Attrgroup_t(0x00000008)
        val ATTR_CMN_OBJTAG = Attrgroup_t(0x00000010)
        val ATTR_CMN_OBJID = Attrgroup_t(0x00000020)
        val ATTR_CMN_OBJPERMANENTID = Attrgroup_t(0x00000040)
        val ATTR_CMN_PAROBJID = Attrgroup_t(0x00000080)
        val ATTR_CMN_SCRIPT = Attrgroup_t(0x00000100)
        val ATTR_CMN_CRTIME = Attrgroup_t(0x00000200)
        val ATTR_CMN_MODTIME = Attrgroup_t(0x00000400)
        val ATTR_CMN_CHGTIME = Attrgroup_t(0x00000800)
        val ATTR_CMN_ACCTIME = Attrgroup_t(0x00001000)
        val ATTR_CMN_BKUPTIME = Attrgroup_t(0x00002000)
        val ATTR_CMN_FNDRINFO = Attrgroup_t(0x00004000)
        val ATTR_CMN_OWNERID = Attrgroup_t(0x00008000)
        val ATTR_CMN_GRPID = Attrgroup_t(0x00010000)
        val ATTR_CMN_ACCESSMASK = Attrgroup_t(0x00020000)
        val ATTR_CMN_FLAGS = Attrgroup_t(0x00040000)
        val ATTR_CMN_GEN_COUNT = Attrgroup_t(0x00080000)
        val ATTR_CMN_DOCUMENT_ID = Attrgroup_t(0x00100000)
        val ATTR_CMN_USERACCESS = Attrgroup_t(0x00200000)
        val ATTR_CMN_EXTENDED_SECURITY = Attrgroup_t(0x00400000)
        val ATTR_CMN_UUID = Attrgroup_t(0x00800000)
        val ATTR_CMN_GRPUUID = Attrgroup_t(0x01000000)
        val ATTR_CMN_FILEID = Attrgroup_t(0x02000000)
        val ATTR_CMN_PARENTID = Attrgroup_t(0x04000000)
        val ATTR_CMN_FULLPATH = Attrgroup_t(0x08000000)
        val ATTR_CMN_ADDEDTIME = Attrgroup_t(0x10000000)
        val ATTR_CMN_ERROR = Attrgroup_t(0x20000000)
        val ATTR_CMN_DATA_PROTECT_FLAGS = Attrgroup_t(0x40000000)
        val ATTR_CMN_RETURNED_ATTRS = Attrgroup_t(0x80000000.toInt())
        val ATTR_CMN_VALIDMASK = Attrgroup_t(0xFFFFFFFF.toInt())
        val ATTR_CMN_SETMASK = Attrgroup_t(0x51C7FF00)
        val ATTR_CMN_VOLSETMASK = Attrgroup_t(0x00006700)

        // Volume attributes
        val ATTR_VOL_FSTYPE = Attrgroup_t(0x00000001)
        val ATTR_VOL_SIGNATURE = Attrgroup_t(0x00000002)
        val ATTR_VOL_SIZE = Attrgroup_t(0x00000004)
        val ATTR_VOL_SPACEFREE = Attrgroup_t(0x00000008)
        val ATTR_VOL_SPACEAVAIL = Attrgroup_t(0x00000010)
        val ATTR_VOL_MINALLOCATION = Attrgroup_t(0x00000020)
        val ATTR_VOL_ALLOCATIONCLUMP = Attrgroup_t(0x00000040)
        val ATTR_VOL_IOBLOCKSIZE = Attrgroup_t(0x00000080)
        val ATTR_VOL_OBJCOUNT = Attrgroup_t(0x00000100)
        val ATTR_VOL_FILECOUNT = Attrgroup_t(0x00000200)
        val ATTR_VOL_DIRCOUNT = Attrgroup_t(0x00000400)
        val ATTR_VOL_MAXOBJCOUNT = Attrgroup_t(0x00000800)
        val ATTR_VOL_MOUNTPOINT = Attrgroup_t(0x00001000)
        val ATTR_VOL_NAME = Attrgroup_t(0x00002000)
        val ATTR_VOL_MOUNTFLAGS = Attrgroup_t(0x00004000)
        val ATTR_VOL_MOUNTEDDEVICE = Attrgroup_t(0x00008000)
        val ATTR_VOL_ENCODINGSUSED = Attrgroup_t(0x00010000)
        val ATTR_VOL_CAPABILITIES = Attrgroup_t(0x00020000)
        val ATTR_VOL_UUID = Attrgroup_t(0x00040000)
        val ATTR_VOL_SPACEUSED = Attrgroup_t(0x800000)
        val ATTR_VOL_QUOTA_SIZE = Attrgroup_t(0x10000000)
        val ATTR_VOL_RESERVED_SIZE = Attrgroup_t(0x20000000)
        val ATTR_VOL_ATTRIBUTES = Attrgroup_t(0x40000000)
        val ATTR_VOL_INFO = Attrgroup_t(0x80000000.toInt())
        val ATTR_VOL_VALIDMASK = Attrgroup_t(0xF007FFFF.toInt())
        val ATTR_VOL_SETMASK = Attrgroup_t(0x80002000.toInt())

        // Directory attributes
        val ATTR_DIR_LINKCOUNT = Attrgroup_t(0x00000001)
        val ATTR_DIR_ENTRYCOUNT = Attrgroup_t(0x00000002)
        val ATTR_DIR_MOUNTSTATUS = Attrgroup_t(0x00000004)
        val ATTR_DIR_ALLOCSIZE = Attrgroup_t(0x00000008)
        val ATTR_DIR_IOBLOCKSIZE = Attrgroup_t(0x00000010)
        val ATTR_DIR_DATALENGTH = Attrgroup_t(0x00000020)
        val ATTR_DIR_VALIDMASK = Attrgroup_t(0x0000003f)
        val ATTR_DIR_SETMASK = Attrgroup_t(0x00000000)

        // File attributes
        val ATTR_FILE_LINKCOUNT = Attrgroup_t(0x00000001)
        val ATTR_FILE_TOTALSIZE = Attrgroup_t(0x00000002)
        val ATTR_FILE_ALLOCSIZE = Attrgroup_t(0x00000004)
        val ATTR_FILE_IOBLOCKSIZE = Attrgroup_t(0x00000008)
        val ATTR_FILE_DEVTYPE = Attrgroup_t(0x00000020)
        val ATTR_FILE_FORKCOUNT = Attrgroup_t(0x00000080)
        val ATTR_FILE_FORKLIST = Attrgroup_t(0x00000100)
        val ATTR_FILE_DATALENGTH = Attrgroup_t(0x00000200)
        val ATTR_FILE_DATAALLOCSIZE = Attrgroup_t(0x00000400)
        val ATTR_FILE_RSRCLENGTH = Attrgroup_t(0x00001000)
        val ATTR_FILE_RSRCALLOCSIZE = Attrgroup_t(0x00002000)
        val ATTR_FILE_VALIDMASK = Attrgroup_t(0x000037FF)
        val ATTR_FILE_SETMASK = Attrgroup_t(0x00000020)

        // Common extension attributes
        val ATTR_CMNEXT_RELPATH = Attrgroup_t(0x00000004)
        val ATTR_CMNEXT_PRIVATESIZE = Attrgroup_t(0x00000008)
        val ATTR_CMNEXT_LINKID = Attrgroup_t(0x00000010)
        val ATTR_CMNEXT_VALIDMASK = Attrgroup_t(0x0000001c)
        val ATTR_CMNEXT_SETMASK = Attrgroup_t(0x00000000)

        // filesystem options
        const val FSOPT_NOFOLLOW = 0x00000001.toLong()
        const val FSOPT_NOINMEMUPDATE = 0x00000002.toLong()
    }

    /**
     * ```c
     * struct statfs { /* when _DARWIN_FEATURE_64_BIT_INODE is defined */
     *     uint32_t    f_bsize;        /* fundamental file system block size */
     *     int32_t     f_iosize;       /* optimal transfer block size */
     *     uint64_t    f_blocks;       /* total data blocks in file system */
     *     uint64_t    f_bfree;        /* free blocks in fs */
     *     uint64_t    f_bavail;       /* free blocks avail to non-superuser */
     *     uint64_t    f_files;        /* total file nodes in file system */
     *     uint64_t    f_ffree;        /* free file nodes in fs */
     *     fsid_t      f_fsid;         /* file system id */
     *     uid_t       f_owner;        /* user that mounted the filesystem */
     *     uint32_t    f_type;         /* type of filesystem */
     *     uint32_t    f_flags;        /* copy of mount exported flags */
     *     uint32_t    f_fssubtype;    /* fs sub-type (flavor) */
     *     char        f_fstypename[MFSTYPENAMELEN];   /* fs type name */
     *     char        f_mntonname[MAXPATHLEN];        /* directory on which mounted */
     *     char        f_mntfromname[MAXPATHLEN];      /* mounted filesystem */
     *     // <- The online docs show an extra uint32 f_flags_ext here, taking up the first reserved slot.
     *     //    The docs on the local machine do not show this.
     *     uint32_t    f_reserved[8];  /* For future use */
     * };
     * ```
     */
    @Structure.FieldOrder(
        "f_bsize", "f_iosize", "f_blocks", "f_bfree", "f_bavail", "f_files", "f_ffree", "f_fsid",
        "f_owner", "f_type", "f_flags", "f_fssubtype", "f_fstypename", "f_mntonname", "f_mntfromname", "f_reserved"
    )
    class Statfs : Structure() {
        @JvmField
        var f_bsize = 0
        @JvmField
        var f_iosize = 0
        @JvmField
        var f_blocks = 0L
        @JvmField
        var f_bfree = 0L
        @JvmField
        var f_bavail = 0L
        @JvmField
        var f_files = 0L
        @JvmField
        var f_ffree = 0L
        @JvmField
        var f_fsid = Fsid_t()
        @JvmField
        var f_owner = Uid_t(0)
        @JvmField
        var f_type = 0
        @JvmField
        var f_flags = 0
        @JvmField
        var f_fssubtype = 0
        @JvmField
        var f_fstypename = ByteArray(MFSTYPENAMELEN)
        @JvmField
        var f_mntonname = ByteArray(MAXPATHLEN)
        @JvmField
        var f_mntfromname = ByteArray(MAXPATHLEN)
        @JvmField
        var f_reserved = IntArray(8)
    }

    /**
     * ```c
     * typedef struct { int32_t val[2]; } fsid_t;
     * ```
     */
    @Structure.FieldOrder("val")
    class Fsid_t : Structure() {
        @JvmField
        var `val` = IntArray(2)

        companion object {
            const val SIZE = 4 * 2
        }
    }

    /**
     * ```c
     * struct attrlist {
     *     u_short     bitmapcount; /* number of attr. bit sets in list */
     *     u_int16_t   reserved;    /* (to maintain 4-byte alignment) */
     *     attrgroup_t commonattr;  /* common attribute group */
     *     attrgroup_t volattr;     /* volume attribute group */
     *     attrgroup_t dirattr;     /* directory attribute group */
     *     attrgroup_t fileattr;    /* file attribute group */
     *     attrgroup_t forkattr;    /* fork attribute group */
     *     };
     * ```
     */
    @Structure.FieldOrder("bitmapcount", "reserved", "commonattr", "volattr", "dirattr", "fileattr", "forkattr")
    class Attrlist : Structure() {
        @JvmField
        var bitmapcount: Short = ATTR_BIT_MAP_COUNT
        @JvmField
        var reserved: Short = 0
        @JvmField
        var commonattr = Attrgroup_t(0)
        @JvmField
        var volattr = Attrgroup_t(0)
        @JvmField
        var dirattr = Attrgroup_t(0)
        @JvmField
        var fileattr = Attrgroup_t(0)
        @JvmField
        var forkattr = Attrgroup_t(0)

        companion object {
            const val SIZE = 4 + ATTR_BIT_MAP_COUNT * Attrgroup_t.SIZE
        }
    }

    /**
     * ```c
     * typedef struct attrreference {
     *     int32_t     attr_dataoffset;
     *     u_int32_t   attr_length;
     *     } attrreference_t;
     * ```
     */
    @Structure.FieldOrder("attr_dataoffset", "attr_length")
    class Attrreference_t(p: Pointer? = null) : Structure(p) {
        @JvmField
        var attr_dataoffset = 0
        @JvmField
        var attr_length = 0

        init {
            if (p != null) {
                read()
            }
        }

        companion object {
            val SIZE = 8
        }
    }

    /**
     * ```c
     * struct timespec
     * {
     *     time_t tv_sec;
     *     long tv_nsec;
     * };
     * ```
     */
    @Structure.FieldOrder("tv_sec", "tv_nsec")
    class Timespec(p: Pointer? = null) : Structure(p) {
        @JvmField
        var tv_sec = Time_t(0)
        @JvmField
        var tv_nsec = NativeLong(0)

        init {
            if (p != null) {
                read()
            }
        }

        /**
         * Converts the value in the `timespec` struct to a Java instant.
         *
         * @return the instant.
         */
        fun toInstant() = UnixEpoch
            .plusSeconds(tv_sec.toLong())
            .plusNanos(tv_nsec.toLong())

        companion object {
            val SIZE = Time_t.SIZE + NativeLong.SIZE

            private val UnixEpoch = ZonedDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC).toInstant()
        }
    }
}