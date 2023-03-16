package garden.ephemeral.macfiles.native

import com.sun.jna.Memory
import com.sun.jna.Platform
import garden.ephemeral.macfiles.alias.*
import garden.ephemeral.macfiles.bookmark.Bookmark
import garden.ephemeral.macfiles.common.types.FourCC
import garden.ephemeral.macfiles.native.internal.*
import garden.ephemeral.macfiles.native.internal.Attrgroup_t
import garden.ephemeral.macfiles.native.internal.FileInfo
import garden.ephemeral.macfiles.native.internal.Fsobj_type_t
import garden.ephemeral.macfiles.native.internal.Size_t
import garden.ephemeral.macfiles.native.internal.SystemB
import java.io.File
import java.text.Normalizer

/**
 * Creates an [Alias] that points at the specified file.
 * Uses various native macOS routines to fill in the data.
 *
 * @param file the file.
 * @return the alias.
 */
fun aliasForFile(file: File): Alias {
    requireMacOS()

    val absoluteFile = file.absoluteFile

    val volumePath = getVolumePath(absoluteFile)
    val volumeInfoBuilder = gatherVolumeInfo(volumePath)
    val targetInfoBuilder = gatherTargetInfo(absoluteFile, volumePath, volumeInfoBuilder.name)

    val alias = Alias.Builder(
        appInfo = FourCC.ZERO,
        version = 2,
        volumeInfo = volumeInfoBuilder,
        targetInfo = targetInfoBuilder,
    )

    return alias.build()
}

private fun getVolumePath(file: File): File {
    // Find the filesystem
    val st = SystemB.Statfs()
    SystemB.INSTANCE.statfs(file.absolutePath.toByteArray(), st)
    // File and folder names in HFS+ are normalized to a form similar to NFD.
    // Must be normalized (NFD->NFC) before use to avoid unicode string comparison issues.
    return File(Normalizer.normalize(st.f_mntonname.decodeToString().trim('\u0000'), Normalizer.Form.NFC))
}

private fun gatherVolumeInfo(volumePath: File): VolumeInfo.Builder {
    // Grab its attributes
    val attrListForVolume = SystemB.Attrlist()
    attrListForVolume.commonattr = SystemB.ATTR_CMN_CRTIME
    attrListForVolume.volattr = SystemB.ATTR_VOL_NAME
    // "You should always pass an attrBufSize that is large enough to accommodate the known size of the
    // attributes in the attribute list (including the leading length field)."
    val attrBufForVolumeLength = 4L +
            SystemB.Timespec.SIZE +
            SystemB.Attrreference_t.SIZE + SystemB.NAME_MAX + 1
    val attrBufForVolume = Memory(attrBufForVolumeLength)
    SystemB.INSTANCE.getattrlist(
        volumePath.absolutePath.toByteArray(),
        attrListForVolume,
        attrBufForVolume,
        Size_t(attrBufForVolumeLength),
        SystemB.FSOPT_NOFOLLOW
    )
    // Decoding the contents...
    if (attrBufForVolume.getInt(0) > attrBufForVolumeLength) {
        throw IllegalStateException("Got attributes longer than the buffer we provided!")
    }
    var attrBufOffset = 4L
    val volumeCreationDate = SystemB.Timespec(attrBufForVolume.share(attrBufOffset)).toInstant()
    attrBufOffset += SystemB.Timespec.SIZE
    val volumeNameRef = SystemB.Attrreference_t(attrBufForVolume.share(attrBufOffset))
    val volumeName = attrBufForVolume
        .getByteArray(attrBufOffset + volumeNameRef.attr_dataoffset, volumeNameRef.attr_length)
        .decodeToString().trim('\u0000')

    // XXX: Hard-coding HFS+ here like the original code being converted - but this is not ideal.
    return VolumeInfo.Builder(
        name = volumeName,
        creationDate = volumeCreationDate,
        fsType = FileSystemType.HFS_PLUS,
        diskType = VolumeType.FIXED_DISK,
        attributeFlags = 0U,
        fsId = "\u0000\u0000",
        posixPath = volumePath.toString()
    )
}

private fun gatherTargetInfo(file: File, volumePath: File, volumeName: String): TargetInfo.Builder {
    // Also grab various attributes of the file
    val attrListForFile = SystemB.Attrlist()
    attrListForFile.commonattr = Attrgroup_t.unionOf(
        SystemB.ATTR_CMN_OBJTYPE,
        SystemB.ATTR_CMN_CRTIME,
        SystemB.ATTR_CMN_FNDRINFO,
        SystemB.ATTR_CMN_FILEID,
        SystemB.ATTR_CMN_PARENTID
    )
    val attrBufForFileLength = 4L +
            Fsobj_type_t.SIZE +
            SystemB.Timespec.SIZE +
            32 + 8 + 8
    val attrBufForFile = Memory(attrBufForFileLength)
    SystemB.INSTANCE.getattrlist(
        file.absolutePath.toByteArray(),
        attrListForFile,
        attrBufForFile,
        Size_t(attrBufForFileLength),
        SystemB.FSOPT_NOFOLLOW
    )
    var attrBufOffset = 4L
    // ATTR_CMN_OBJTYPE
    val file_objtype = Fsobj_type_t(attrBufForFile.getInt(attrBufOffset))
    attrBufOffset += Fsobj_type_t.SIZE
    // ATTR_CMN_CRTIME
    val creationDate = SystemB.Timespec(attrBufForFile.share(attrBufOffset)).toInstant()
    attrBufOffset += SystemB.Timespec.SIZE

    // Interrupt the nice program layout to figure out what kind of item it is,
    // because the contents of ATTR_CMN_FNDRINFO vary depending on which type it is. :/
    val kind = if (file_objtype.toInt() == Fsobj_type_t.VDIR) Kind.FOLDER else Kind.FILE

    // ATTR_CMN_FNDRINFO
    val fileInfo = if (kind == Kind.FILE) {
        FileInfo(attrBufForFile.share(attrBufOffset))
    } else {
        // In this branch we have a FolderInfo instead. It's the same size, but doesn't contain
        // the information we want.
        null
    }
    attrBufOffset += FileInfo.SIZE + ExtendedFileInfo.SIZE

    // ATTR_CMN_FILEID
    val cnid = attrBufForFile.getLong(attrBufOffset).asCnid()
    attrBufOffset += 8
    // ATTR_CMN_PARENTID
    val folderCnid = attrBufForFile.getLong(attrBufOffset).asCnid()

    val parentFolder = file.absoluteFile.parentFile
    val filename = file.name
    val folderName = parentFolder.name

    val creatorCode = fileInfo?.let { f -> FourCC.fromInt(f.fileCreator.toInt()) } ?: FourCC.ZERO
    val typeCode = fileInfo?.let { f -> FourCC.fromInt(f.fileType.toInt()) } ?: FourCC.ZERO

    // Construct the Carbon and CNID paths
    val carbonPathElements = mutableListOf<String>()
    val cnidPath = mutableListOf<UInt>()
    val relativePath = file.relativeTo(volumePath)

    var temp: File? = file.relativeTo(volumePath)
    while (temp != null) {
        val attrListForAncestor = SystemB.Attrlist()
        attrListForAncestor.commonattr = SystemB.ATTR_CMN_FILEID
        val attrBufForAncestorSize = 8L
        val attrBufForAncestor = Memory(attrBufForAncestorSize)
        SystemB.INSTANCE.getattrlist(
            volumePath.resolve(temp).absolutePath.toByteArray(),
            attrListForAncestor,
            attrBufForAncestor,
            Size_t(attrBufForAncestorSize),
            SystemB.FSOPT_NOFOLLOW
        )
        cnidPath.add(attrBufForAncestor.getLong(0L).asCnid())
        carbonPathElements.add(0, temp.name.replace(':', '/'))
        temp = temp.parentFile
    }

    val carbonPath = volumeName + ":" + carbonPathElements.joinToString(":\u0000")

    // Leave off the initial '/' if vol_path is '/' (no idea why)
    val posixPath = if (volumePath.toString() == "/") "$relativePath" else "/$relativePath"

    return TargetInfo.Builder(
        name = filename,
        kind = kind,
        cnid = cnid,
        folderCnid = folderCnid,
        creationDate = creationDate,
        creatorCode = creatorCode,
        typeCode = typeCode,
        folderName = folderName,
        cnidPath = cnidPath,
        carbonPath = carbonPath,
        posixPath = posixPath
    )
}

fun bookmarkForFile(path: File): Bookmark {
    requireMacOS()

    /*
     @classmethod
     def for_file(cls, path):
         """Construct a :class:`Bookmark` for a given file."""

         # Find the filesystem
         st = osx.statfs(path)
         vol_path = st.f_mntonname.decode("utf-8")

         # Grab its attributes
         attrs = [
             osx.ATTR_CMN_CRTIME,
             osx.ATTR_VOL_SIZE | osx.ATTR_VOL_NAME | osx.ATTR_VOL_UUID,
             0,
             0,
             0,
             ]
         volinfo = osx.getattrlist(vol_path, attrs, 0)

         vol_crtime = volinfo[0]
         vol_size = volinfo[1]
         vol_name = volinfo[2]
         vol_uuid = volinfo[3]

         # Also grab various attributes of the file
         attrs = [
             (osx.ATTR_CMN_OBJTYPE | osx.ATTR_CMN_CRTIME | osx.ATTR_CMN_FILEID),
             0,
             0,
             0,
             0,
         ]
         info = osx.getattrlist(path, attrs, osx.FSOPT_NOFOLLOW)

         cnid = info[2]
         crtime = info[1]

         if info[0] == osx.VREG:
             flags = kCFURLResourceIsRegularFile
         elif info[0] == osx.VDIR:
             flags = kCFURLResourceIsDirectory
         elif info[0] == osx.VLNK:
             flags = kCFURLResourceIsSymbolicLink
         else:
             flags = kCFURLResourceIsRegularFile

         dirname, filename = os.path.split(path)

         relcount = 0
         if not os.path.isabs(dirname):
             curdir = os.getcwd()
             head, tail = os.path.split(curdir)
             relcount = 0
             while head and tail:
                 relcount += 1
                 head, tail = os.path.split(head)
             dirname = os.path.join(curdir, dirname)

         # ?? foldername = os.path.basename(dirname)

         rel_path = os.path.relpath(path, vol_path)

         # Build the path arrays
         name_path = []
         cnid_path = []
         head, tail = os.path.split(rel_path)
         if not tail:
             head, tail = os.path.split(head)
         while head or tail:
             if head:
                 attrs = [osx.ATTR_CMN_FILEID, 0, 0, 0, 0]
                 info = osx.getattrlist(os.path.join(vol_path, head), attrs, 0)
                 cnid_path.insert(0, info[0])
                 head, tail = os.path.split(head)
                 name_path.insert(0, tail)
             else:
                 head, tail = os.path.split(head)
         name_path.append(filename)
         cnid_path.append(cnid)

         url_lengths = [relcount, len(name_path) - relcount]

         fileprops = Data(struct.pack(b"<QQQ", flags, 0x0F, 0))
         volprops = Data(
             struct.pack(
                 b"<QQQ",
                 0x81 | kCFURLVolumeSupportsPersistentIDs,
                 0x13EF | kCFURLVolumeSupportsPersistentIDs,
                 0,
                 )
         )

         toc = {
             kBookmarkPath: name_path,
             kBookmarkCNIDPath: cnid_path,
             kBookmarkFileCreationDate: crtime,
             kBookmarkFileProperties: fileprops,
             kBookmarkContainingFolder: len(name_path) - 2,
             kBookmarkVolumePath: vol_path,
             kBookmarkVolumeIsRoot: vol_path == "/",
             kBookmarkVolumeURL: URL("file://" + vol_path),
             kBookmarkVolumeName: vol_name,
             kBookmarkVolumeSize: vol_size,
             kBookmarkVolumeCreationDate: vol_crtime,
             kBookmarkVolumeUUID: str(vol_uuid).upper(),
             kBookmarkVolumeProperties: volprops,
             kBookmarkCreationOptions: 512,
             kBookmarkWasFileReference: True,
             kBookmarkUserName: "unknown",
             kBookmarkUID: 99,
         }

         if relcount:
             toc[kBookmarkURLLengths] = url_lengths

         return Bookmark([(1, toc)])
      */
    TODO("Native code for bookmark generation")
}

private fun requireMacOS() {
    if (!Platform.isMac()) {
        throw UnsupportedOperationException("Not implemented on this platform (requires native macOS support)")
    }
}

private fun Long.asCnid() = if (this >= 0 && this <= UInt.MAX_VALUE.toLong()) {
    this.toUInt()
} else {
    0xFFFFFFFFU
}
