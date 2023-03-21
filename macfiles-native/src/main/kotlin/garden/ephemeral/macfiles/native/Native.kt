package garden.ephemeral.macfiles.native

import com.sun.jna.Platform
import garden.ephemeral.macfiles.alias.*
import garden.ephemeral.macfiles.bookmark.Bookmark
import garden.ephemeral.macfiles.bookmark.BookmarkKeys
import garden.ephemeral.macfiles.bookmark.types.URL
import garden.ephemeral.macfiles.bookmark.types.UUID
import garden.ephemeral.macfiles.common.io.Block
import garden.ephemeral.macfiles.common.types.FourCC
import garden.ephemeral.macfiles.native.internal.*
import garden.ephemeral.macfiles.native.internal.FileInfo
import garden.ephemeral.macfiles.native.internal.Fsobj_type_t
import garden.ephemeral.macfiles.native.internal.SystemB
import java.io.File
import java.text.Normalizer
import java.time.Instant

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

private fun gatherVolumeInfo(volumePath: File): VolumeInfo.Builder {
    // Grab its attributes
    val attributes = getAttrList(
        volumePath,
        commonAttributes = listOf(SystemB.ATTR_CMN_CRTIME),
        volumeAttributes = listOf(SystemB.ATTR_VOL_NAME)
    )
    val volumeCreationDate = attributes[0] as Instant
    val volumeName = attributes[1] as String

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
    // Grab various attributes of the file
    val attributes = getAttrList(
        file,
        commonAttributes = listOf(
            SystemB.ATTR_CMN_OBJTYPE, SystemB.ATTR_CMN_CRTIME, SystemB.ATTR_CMN_FNDRINFO,
            SystemB.ATTR_CMN_FILEID, SystemB.ATTR_CMN_PARENTID
        )
    )
    val fileObjType = attributes[0] as Fsobj_type_t
    val creationDate = attributes[1] as Instant
    @Suppress("UNCHECKED_CAST")
    val finderInfo = attributes[2] as Pair<FileInfo, ExtendedFileInfo>
    val cnid = (attributes[3] as Long).asCnid()
    val folderCnid = (attributes[4] as Long).asCnid()

    val kind = if (fileObjType.toInt() == Fsobj_type_t.VDIR) Kind.FOLDER else Kind.FILE

    val parentFolder = file.absoluteFile.parentFile
    val filename = file.name
    val folderName = parentFolder.name

    val creatorCode: FourCC
    val typeCode: FourCC
    if (kind == Kind.FILE) {
        val fileInfo = finderInfo.first
        creatorCode = FourCC.fromInt(fileInfo.fileCreator.toInt())
        typeCode = FourCC.fromInt(fileInfo.fileType.toInt())
    } else {
        creatorCode = FourCC.ZERO
        typeCode = FourCC.ZERO
    }

    // Construct the Carbon and CNID paths
    val carbonPathElements = mutableListOf<String>()
    val cnidPath = mutableListOf<UInt>()
    val relativePath = file.relativeTo(volumePath)


    var temp: File? = file.relativeTo(volumePath)
    while (temp != null) {
        val attributesForAncestor = getAttrList(
            volumePath.resolve(temp),
            commonAttributes = listOf(SystemB.ATTR_CMN_FILEID)
        )
        cnidPath.add((attributesForAncestor[0] as Long).asCnid())
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

/**
 * Creates a [Bookmark] that points at the specified file.
 * Uses various native macOS routines to fill in the data.
 *
 * @param file the file.
 * @return the bookmark.
 */
fun bookmarkForFile(file: File): Bookmark {
    requireMacOS()

    val absoluteFile = file.absoluteFile

    val volumePath = getVolumePath(absoluteFile)

    val volumeAttributes = getAttrList(
        volumePath,
        commonAttributes = listOf(SystemB.ATTR_CMN_CRTIME),
        volumeAttributes = listOf(SystemB.ATTR_VOL_SIZE, SystemB.ATTR_VOL_NAME, SystemB.ATTR_VOL_UUID)
    )
    val volumeCreationTime = volumeAttributes[0] as Instant
    val volumeSize = volumeAttributes[1] as Long
    val volumeName = volumeAttributes[2] as String
    val volumeUuid = volumeAttributes[3] as UUID

    val fileAttributes = getAttrList(
        absoluteFile,
        commonAttributes = listOf(SystemB.ATTR_CMN_OBJTYPE, SystemB.ATTR_CMN_CRTIME)
    )
    val fileObjType = fileAttributes[0] as Fsobj_type_t
    val fileCreationTime = fileAttributes[1] as Instant

    val flags = when (fileObjType.toInt()) {
        Fsobj_type_t.VREG -> kCFURLResourceIsRegularFile
        Fsobj_type_t.VDIR -> kCFURLResourceIsDirectory
        Fsobj_type_t.VLNK -> kCFURLResourceIsSymbolicLink
        else -> kCFURLResourceIsRegularFile
    }

    // I guess relcount contains the number of elements from the root to the
    // current working directory? So that you can take the absolute path stored
    // in the bookmark and recover the original relative path from it.
    // I have no idea what the purpose is.
    var relativeCount = 0
    if (!file.isRooted) {
        var temp: File? = File(System.getProperty("user.dir"))
        while (temp != null) {
            relativeCount++
            temp = temp.parentFile
        }
    }

    // Build the path arrays
    val namePath = mutableListOf<String>()
    val cnidPath = mutableListOf<UInt>()
    var temp: File? = absoluteFile.relativeTo(volumePath)
    while (temp != null) {
        cnidPath.add(0, getCnidFor(volumePath.resolve(temp)))
        namePath.add(0, temp.name)
        temp = temp.parentFile
    }

    val url_lengths = listOf(relativeCount, namePath.size - relativeCount)

    // Many esoteric magic numbers ahead. Was copied from code which also did not explain them.

    val filePropertiesBlob = Block.create(24) { stream ->
        stream.writeLongLE(flags)
        stream.writeLongLE(0x0F)
        stream.writeLongLE(0)
    }.toBlob()

    val volumePropertiesBlob = Block.create(24) { stream ->
        stream.writeLongLE(0x81L.or(kCFURLVolumeSupportsPersistentIDs))
        stream.writeLongLE(0x13EFL.or(kCFURLVolumeSupportsPersistentIDs))
        stream.writeLongLE(0)
    }.toBlob()

    return Bookmark.build {
        put(BookmarkKeys.kBookmarkPath, namePath)
        put(BookmarkKeys.kBookmarkCNIDPath, cnidPath)
        put(BookmarkKeys.kBookmarkFileCreationDate, fileCreationTime)
        put(BookmarkKeys.kBookmarkFileProperties, filePropertiesBlob)
        put(BookmarkKeys.kBookmarkContainingFolder, namePath.size - 2)
        put(BookmarkKeys.kBookmarkVolumePath, volumePath.toString())
        put(BookmarkKeys.kBookmarkVolumeIsRoot, volumePath.toString() == "/")
        put(BookmarkKeys.kBookmarkVolumeURL, URL.Absolute("file://$volumePath"))
        put(BookmarkKeys.kBookmarkVolumeName, volumeName)
        put(BookmarkKeys.kBookmarkVolumeSize, volumeSize)
        put(BookmarkKeys.kBookmarkVolumeCreationDate, volumeCreationTime)
        put(BookmarkKeys.kBookmarkVolumeUUID, volumeUuid)
        put(BookmarkKeys.kBookmarkVolumeProperties, volumePropertiesBlob)
        put(BookmarkKeys.kBookmarkCreationOptions, 512)
        put(BookmarkKeys.kBookmarkWasFileReference, true)
        put(BookmarkKeys.kBookmarkUserName, "unknown")
        put(BookmarkKeys.kBookmarkUID, 99)

        if (relativeCount > 0) {
            put(BookmarkKeys.kBookmarkURLLengths, url_lengths)
        }
    }
}

private fun getVolumePath(file: File): File {
    // Find the filesystem
    val st = SystemB.Statfs()
    SystemB.INSTANCE.statfs(file.absolutePath.toByteArray(), st)
    // File and folder names in HFS+ are normalized to a form similar to NFD.
    // Must be normalized (NFD->NFC) before use to avoid unicode string comparison issues.
    return File(Normalizer.normalize(st.f_mntonname.decodeToString().trim('\u0000'), Normalizer.Form.NFC))
}

private fun requireMacOS() {
    if (!Platform.isMac()) {
        throw UnsupportedOperationException("Not implemented on this platform (requires native macOS support)")
    }
}

fun getCnidFor(file: File): UInt {
    val attributesForAncestor = getAttrList(
        file,
        commonAttributes = listOf(SystemB.ATTR_CMN_FILEID)
    )
    return (attributesForAncestor[0] as Long).asCnid()
}

private fun Long.asCnid() = if (this >= 0 && this <= UInt.MAX_VALUE.toLong()) {
    this.toUInt()
} else {
    0xFFFFFFFFU
}
