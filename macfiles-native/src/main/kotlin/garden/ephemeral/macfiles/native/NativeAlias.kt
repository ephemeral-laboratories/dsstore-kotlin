package garden.ephemeral.macfiles.native

import garden.ephemeral.macfiles.alias.*
import garden.ephemeral.macfiles.common.types.*
import garden.ephemeral.macfiles.native.internal.*
import java.io.*
import java.time.*

/**
 * Creates an [Alias] that points at the specified file.
 * Uses various native macOS routines to fill in the data.
 *
 * @param file the file.
 * @return the alias.
 * @throws NoSuchFileException if the file does not exist.
 */
@Throws(NoSuchFileException::class)
fun aliasForFile(file: File): Alias {
    requireMacOS()
    checkFileExists(file)

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
        cnidPath.add(getCnidFor(volumePath.resolve(temp)))
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

private fun getCnidFor(file: File): UInt {
    val attributesForAncestor = getAttrList(
        file,
        commonAttributes = listOf(SystemB.ATTR_CMN_FILEID)
    )
    return (attributesForAncestor[0] as Long).asCnid()
}

/**
 * Only for Alias files, because it's an ancient format which can only store 32-bit
 * CNIDs, we have this workaround to reject values which cannot possibly be stored.
 */
private fun Long.asCnid() = if (this >= 0 && this <= UInt.MAX_VALUE.toLong()) {
    this.toUInt()
} else {
    0xFFFFFFFFU
}
