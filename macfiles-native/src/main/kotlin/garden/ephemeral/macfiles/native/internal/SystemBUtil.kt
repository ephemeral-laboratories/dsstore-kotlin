package garden.ephemeral.macfiles.native.internal

import com.sun.jna.*
import java.io.*
import java.text.*

/**
 * Convenience method around [SystemB.statfs64] to get just the volume path.
 *
 * @param file the file to query the volume path for.
 * @return the path to the volume containing the file.
 */
internal fun getVolumePath(file: File): File {
    // Find the filesystem
    val st = SystemB.Statfs()
    SystemB.INSTANCE.statfs64(file.absolutePath.toByteArray(), st)
    // File and folder names in HFS+ are normalized to a form similar to NFD.
    // Must be normalized (NFD->NFC) before use to avoid unicode string comparison issues.
    return File(Normalizer.normalize(st.f_mntonname.decodeToString().trim('\u0000'), Normalizer.Form.NFC))
}

/**
 * Convenience method around [SystemB.getattrlist] which takes care of packaging the
 * input struct, determining the correct size for the buffer, and decoding the buffer
 * into more useful objects.
 *
 * @param file the file to query the attributes for.
 * @param commonAttributes common attributes to return.
 * @param volumeAttributes volume attributes to return.
 * @param directoryAttributes directory attributes to return.
 * @param fileAttributes file attributes to return.
 * @param forkAttributes fork attributes to return.
 * @return the attribute values, in the same order as the queried attributes.
 *         Common attributes come before volume attributes, and so on.
 *         Within a given attribute set the values are returned in numeric order, lowest to highest.
 */
internal fun getAttrList(
    file: File,
    commonAttributes: List<Attrgroup_t> = emptyList(),
    volumeAttributes: List<Attrgroup_t> = emptyList(),
    directoryAttributes: List<Attrgroup_t> = emptyList(),
    fileAttributes: List<Attrgroup_t> = emptyList(),
    forkAttributes: List<Attrgroup_t> = emptyList()
): List<Any> {
    val attrList = SystemB.Attrlist()
    attrList.commonattr = Attrgroup_t.unionOf(commonAttributes)
    attrList.volattr = Attrgroup_t.unionOf(volumeAttributes)
    attrList.dirattr = Attrgroup_t.unionOf(directoryAttributes)
    attrList.fileattr = Attrgroup_t.unionOf(fileAttributes)
    attrList.forkattr = Attrgroup_t.unionOf(forkAttributes)

    // "You should always pass an attrBufSize that is large enough to accommodate the known size of the
    // attributes in the attribute list (including the leading length field)."
    // i.e. API gives you no way to ask it what size it needs.
    val attrBufferSize = 4L +
            commonAttributes.sumOf(::calcCommonAttributeMaxSize) +
            volumeAttributes.sumOf(::calcVolumeAttributeMaxSize) +
            directoryAttributes.sumOf(::calcDirectoryAttributeMaxSize) +
            fileAttributes.sumOf(::calcFileAttributeMaxSize) +
            forkAttributes.sumOf(::calcForkAttributeMaxSize)
    val attrBuffer = Memory(attrBufferSize)

    SystemB.INSTANCE.getattrlist(
        file.absolutePath.toByteArray(),
        attrList,
        attrBuffer,
        Size_t(attrBufferSize),
        SystemB.FSOPT_NOFOLLOW
    )

    return buildList {
        var offset = 4L

        fun decodeAttributes(attributes: List<Attrgroup_t>, decodeFunction: (Attrgroup_t, Pointer) -> Pair<Any, Int>) {
            attributes.sortedWith(Attrgroup_t.COMPARATOR).forEach { attribute ->
                val (decodedValue, bytesRead) = decodeFunction(attribute, attrBuffer.share(offset))
                add(decodedValue)
                offset += bytesRead
            }
        }

        decodeAttributes(commonAttributes, ::decodeCommonAttribute)
        decodeAttributes(volumeAttributes, ::decodeVolumeAttribute)
        decodeAttributes(directoryAttributes, ::decodeDirectoryAttribute)
        decodeAttributes(fileAttributes, ::decodeFileAttribute)
        decodeAttributes(forkAttributes, ::decodeForkAttribute)
    }
}

private fun calcCommonAttributeMaxSize(attribute: Attrgroup_t) = when (attribute) {
    SystemB.ATTR_CMN_OBJTYPE -> Fsobj_type_t.SIZE
    SystemB.ATTR_CMN_CRTIME -> SystemB.Timespec.SIZE
    SystemB.ATTR_CMN_FNDRINFO -> 32
    SystemB.ATTR_CMN_FILEID,
    SystemB.ATTR_CMN_PARENTID -> 8
    else -> throw UnsupportedOperationException("Unsupported attribute: $attribute")
}

private fun decodeCommonAttribute(attribute: Attrgroup_t, pointer: Pointer): Pair<Any, Int> = when (attribute) {
    SystemB.ATTR_CMN_OBJTYPE -> Pair(Fsobj_type_t(pointer.getInt(0L)), Fsobj_type_t.SIZE)
    SystemB.ATTR_CMN_CRTIME -> Pair(SystemB.Timespec(pointer).toInstant(), SystemB.Timespec.SIZE)
    SystemB.ATTR_CMN_FNDRINFO -> {
        // XXX: Technically this is sometimes a FolderInfo instead, but we never use that.
        //      If we had to do it, how would we tell the difference between the two?
        val decoded = Pair(FileInfo(pointer.share(0L)), ExtendedFileInfo(pointer.share(0L)))
        Pair(decoded, 32)
    }
    SystemB.ATTR_CMN_FILEID,
    SystemB.ATTR_CMN_PARENTID -> Pair(pointer.getLong(0L), 8)
    else -> throw UnsupportedOperationException("Unsupported attribute: $attribute")
}

private fun calcVolumeAttributeMaxSize(attribute: Attrgroup_t) = when (attribute) {
    SystemB.ATTR_VOL_SIZE,
    SystemB.ATTR_VOL_SPACEFREE,
    SystemB.ATTR_VOL_SPACEAVAIL,
    SystemB.ATTR_VOL_SPACEUSED,
    SystemB.ATTR_VOL_MINALLOCATION,
    SystemB.ATTR_VOL_ALLOCATIONCLUMP,
    SystemB.ATTR_VOL_QUOTA_SIZE,
    SystemB.ATTR_VOL_RESERVED_SIZE -> Off_t.SIZE
    SystemB.ATTR_VOL_NAME -> SystemB.Attrreference_t.SIZE + SystemB.NAME_MAX + 1
    SystemB.ATTR_VOL_UUID -> Uuid_t.SIZE
    else -> throw UnsupportedOperationException("Unsupported attribute: $attribute")
}

private fun decodeVolumeAttribute(attribute: Attrgroup_t, pointer: Pointer): Pair<Any, Int> = when (attribute) {
    SystemB.ATTR_VOL_SIZE,
    SystemB.ATTR_VOL_SPACEFREE,
    SystemB.ATTR_VOL_SPACEAVAIL,
    SystemB.ATTR_VOL_SPACEUSED,
    SystemB.ATTR_VOL_MINALLOCATION,
    SystemB.ATTR_VOL_ALLOCATIONCLUMP,
    SystemB.ATTR_VOL_QUOTA_SIZE,
    SystemB.ATTR_VOL_RESERVED_SIZE -> Pair(Off_t(pointer.getLong(0L)).toLong(), Off_t.SIZE)
    SystemB.ATTR_VOL_NAME -> {
        val ref = SystemB.Attrreference_t(pointer)
        val str = pointer
            .getByteArray(ref.attr_dataoffset.toLong(), ref.attr_length)
            .decodeToString().trim('\u0000')
        Pair(str, SystemB.Attrreference_t.SIZE)
    }
    SystemB.ATTR_VOL_UUID -> Pair(Uuid_t(pointer.share(0L)).toUuid(), Uuid_t.SIZE)
    else -> throw UnsupportedOperationException("Unsupported attribute: $attribute")
}

private fun calcDirectoryAttributeMaxSize(attribute: Attrgroup_t): Int =
    throw UnsupportedOperationException("Unsupported attribute: $attribute")

private fun decodeDirectoryAttribute(attribute: Attrgroup_t, pointer: Pointer): Pair<Any, Int> =
    throw UnsupportedOperationException("Unsupported attribute: $attribute")

private fun calcFileAttributeMaxSize(attribute: Attrgroup_t): Int =
    throw UnsupportedOperationException("Unsupported attribute: $attribute")

private fun decodeFileAttribute(attribute: Attrgroup_t, pointer: Pointer): Pair<Any, Int> =
    throw UnsupportedOperationException("Unsupported attribute: $attribute")

private fun calcForkAttributeMaxSize(attribute: Attrgroup_t): Int =
    throw UnsupportedOperationException("Unsupported attribute: $attribute")

private fun decodeForkAttribute(attribute: Attrgroup_t, pointer: Pointer): Pair<Any, Int> =
    throw UnsupportedOperationException("Unsupported attribute: $attribute")
