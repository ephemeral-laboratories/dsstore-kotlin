package garden.ephemeral.macfiles.native

import garden.ephemeral.macfiles.bookmark.Bookmark
import garden.ephemeral.macfiles.bookmark.BookmarkKeys
import garden.ephemeral.macfiles.bookmark.types.URL
import garden.ephemeral.macfiles.bookmark.types.UUID
import garden.ephemeral.macfiles.common.io.Block
import garden.ephemeral.macfiles.native.internal.*
import java.io.File
import java.time.Instant

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
    val cnidPath = mutableListOf<Long>()
    var temp: File? = absoluteFile.relativeTo(volumePath)
    while (temp != null) {
        cnidPath.add(0, getCnidFor(volumePath.resolve(temp)))
        namePath.add(0, temp.name)
        temp = temp.parentFile
    }

    val urlLengths = listOf(relativeCount, namePath.size - relativeCount)

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
            put(BookmarkKeys.kBookmarkURLLengths, urlLengths)
        }
    }
}

private fun getCnidFor(file: File): Long {
    val attributesForAncestor = getAttrList(
        file,
        commonAttributes = listOf(SystemB.ATTR_CMN_FILEID)
    )
    return attributesForAncestor[0] as Long
}
