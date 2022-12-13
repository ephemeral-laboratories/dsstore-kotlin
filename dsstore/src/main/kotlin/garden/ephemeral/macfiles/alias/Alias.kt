package garden.ephemeral.macfiles.alias

import garden.ephemeral.macfiles.common.MacTimeUtils
import garden.ephemeral.macfiles.common.io.Block
import garden.ephemeral.macfiles.common.io.DataInput
import garden.ephemeral.macfiles.common.io.DataOutput
import garden.ephemeral.macfiles.common.types.Blob
import garden.ephemeral.macfiles.common.types.FourCC
import java.nio.charset.StandardCharsets
import java.time.Instant

/**
 * Representation of a macOS Alias file.
 *
 * @param appInfo application-specific information.
 * @param version the alias format version (we support versions 2 and 3).
 * @param volume info about the target's volume.
 * @param target info about the target.
 * @param unrecognised a list of extra tags we didn't recognise.
 */
data class Alias(
    val appInfo: FourCC,
    val version: Short,
    val volume: VolumeInfo,
    val target: TargetInfo,
    val unrecognised: List<Pair<Short, Blob>> = listOf()
) {
    fun toBlob() = Block.create(calculateSize(), ::writeTo).toBlob()

    fun writeTo(stream: DataOutput) {
        // We'll come back and fix the length when we're done
        val startPosition = stream.position()
        AliasHeader(appInfo, 0, version).writeTo(stream)

        val aliasRecord = if (version == 2.toShort()) {
            AliasRecordV2.forAlias(this)
        } else { // version 3
            AliasRecordV3.forAlias(this)
        }
        aliasRecord.writeTo(stream)

        // Excuse the odd order; we're copying Finder

        target.folderName?.let { folderName ->
            writeRecord(stream, Tag.CARBON_FOLDER_NAME, folderName.replace(':', '/'))
        }

        writeRecord(stream, Tag.HIGH_RES_VOLUME_CREATION_DATE, volume.creationDate)
        writeRecord(stream, Tag.HIGH_RES_CREATION_DATE, target.creationDate)

        // TAG_CNID_PATH
        target.cnidPath?.let { cnidPath ->
            stream.writeShort(Tag.CNID_PATH.value)
            stream.writeShort((4 * cnidPath.size).toShort())
            cnidPath.forEach(stream::writeInt)
        }

        target.carbonPath?.let { carbonPath ->
            writeRecord(stream, Tag.CARBON_PATH, carbonPath)
        }

        volume.appleShareInfo?.let { appleShareInfo ->
            appleShareInfo.zone?.let { zone ->
                writeRecord(stream, Tag.APPLESHARE_ZONE, zone)
            }
            appleShareInfo.zone?.let { server ->
                writeRecord(stream, Tag.APPLESHARE_SERVER_NAME, server)
            }
            appleShareInfo.zone?.let { username ->
                writeRecord(stream, Tag.APPLESHARE_USERNAME, username)
            }
        }

        volume.driverName?.let { driverName ->
            writeRecord(stream, Tag.DRIVER_NAME, driverName)
        }

        volume.networkMountInfo?.let { networkMountInfo ->
            writeRecord(stream, Tag.NETWORK_MOUNT_INFO, networkMountInfo)
        }

        volume.dialupInfo?.let { dialupInfo ->
            writeRecord(stream, Tag.DIALUP_INFO, dialupInfo)
        }

        writeUStrRecord(stream, Tag.UNICODE_FILENAME, target.name)

        writeUStrRecord(stream, Tag.UNICODE_VOLUME_NAME, volume.name)

        target.posixPath?.let { posixPath ->
            writeRecord(stream, Tag.POSIX_PATH, posixPath)
        }

        volume.posixPath?.let { posixPath ->
            writeRecord(stream, Tag.POSIX_PATH_TO_MOUNTPOINT, posixPath)
        }

        volume.diskImageAlias?.let { diskImageAlias ->
            writeRecord(stream, Tag.RECURSIVE_ALIAS_OF_DISK_IMAGE, diskImageAlias.toBlob())
        }

        target.userHomePrefixLen?.let { userHomePrefixLen ->
            stream.writeShort(Tag.USER_HOME_LENGTH_PREFIX.value)
            stream.writeShort(2)
            stream.writeShort(userHomePrefixLen)
        }

        unrecognised.forEach { (tag, blob) ->
            val blobSize = blob.size
            stream.writeShort(tag)
            stream.writeShort(blobSize.toShort())
            stream.writeBlob(blob)
            if (blobSize % 2 != 0) {
                stream.skip(1)
            }
        }

        // end of records marker
        stream.writeShort(-1)
        stream.writeShort(0)

        // fix length at start
        val endPosition = stream.position()
        val totalSize = endPosition - startPosition
        stream.position(startPosition + 4)
        stream.writeShort(totalSize.toShort())
        stream.position(endPosition)
    }

    fun calculateSize(): Int {
        var size = AliasHeader.SIZE

        size += if (version == 2.toShort()) {
            AliasRecordV2.SIZE
        } else { // version 3
            AliasRecordV3.SIZE
        }

        // TAG_CARBON_FOLDER_NAME
        target.folderName?.let { folderName ->
            size += calculateStringRecordSize(folderName)
        }

        // TAG_HIGH_RES_VOLUME_CREATION_DATE + TAG_HIGH_RES_CREATION_DATE
        size += 24

        // TAG_CNID_PATH
        target.cnidPath?.let { cnidPath ->
            size = 4 + 4 * cnidPath.size
        }

        // TAG_CARBON_PATH
        target.carbonPath?.let { carbonPath ->
            size += calculateStringRecordSize(carbonPath)
        }

        volume.appleShareInfo?.let { appleShareInfo ->
            // TAG_APPLESHARE_ZONE
            appleShareInfo.zone?.let { zone ->
                size += calculateStringRecordSize(zone)
            }
            // TAG_APPLESHARE_SERVER_NAME
            appleShareInfo.zone?.let { server ->
                size += calculateStringRecordSize(server)
            }
            // TAG_APPLESHARE_USERNAME
            appleShareInfo.zone?.let { username ->
                size += calculateStringRecordSize(username)
            }
        }

        // TAG_DRIVER_NAME
        volume.driverName?.let { driverName ->
            size += calculateStringRecordSize(driverName)
        }

        // TAG_NETWORK_MOUNT_INFO
        volume.networkMountInfo?.let { networkMountInfo ->
            size += calculateBlobRecordSize(networkMountInfo)
        }

        // TAG_DIALUP_INFO
        volume.dialupInfo?.let { dialupInfo ->
            size += calculateBlobRecordSize(dialupInfo)
        }

        // TAG_UNICODE_FILENAME
        size += 6 + target.name.length * 2

        // TAG_UNICODE_VOLUME_NAME
        size += 6 + volume.name.length * 2

        // TAG_POSIX_PATH
        target.posixPath?.let { posixPath ->
            size += calculateStringRecordSize(posixPath)
        }

        // TAG_POSIX_PATH_TO_MOUNTPOINT
        volume.posixPath?.let { posixPath ->
            size += calculateStringRecordSize(posixPath)
        }

        // TAG_RECURSIVE_ALIAS_OF_DISK_IMAGE
        volume.diskImageAlias?.let { diskImageAlias ->
            val diskImageAliasSize = diskImageAlias.calculateSize()
            size += 4 + diskImageAliasSize
            if (diskImageAliasSize % 2 != 0) {
                size++
            }
        }

        // TAG_USER_HOME_LENGTH_PREFIX
        if (target.userHomePrefixLen != null) {
            size += 6
        }

        unrecognised.forEach { (_, blob) ->
            size += calculateBlobRecordSize(blob)
        }

        // end marker
        size += 4

        return size
    }

    private fun writeRecord(stream: DataOutput, tag: Tag, value: String) {
        val valueBytes = value.toByteArray()
        stream.writeShort(tag.value)
        stream.writeShort(valueBytes.size.toShort())
        stream.writeString(valueBytes.size, value, StandardCharsets.UTF_8)
        if (valueBytes.size % 2 != 0) {
            stream.skip(1)
        }
    }

    private fun writeUStrRecord(stream: DataOutput, tag: Tag, value: String) {
        stream.writeShort(tag.value)
        stream.writeShort((value.length * 2 + 2).toShort())
        stream.writeShort(value.length.toShort())
        stream.writeString(value, StandardCharsets.UTF_16BE)
    }

    private fun writeRecord(stream: DataOutput, tag: Tag, value: Blob) {
        val valueSize = value.size
        stream.writeShort(tag.value)
        stream.writeShort(valueSize.toShort())
        stream.writeBlob(value)
        if (valueSize % 2 != 0) {
            stream.skip(1)
        }
    }

    private fun writeRecord(stream: DataOutput, tag: Tag, value: Instant) {
        stream.writeShort(tag.value)
        stream.writeShort(8)
        stream.writeLong(MacTimeUtils.encodeHighResInstant(value))
    }

    private fun calculateStringRecordSize(string: String) = padToMultipleOf2(4 + string.toByteArray().size)
    private fun calculateBlobRecordSize(blob: Blob) = padToMultipleOf2(4 + blob.size)
    private fun padToMultipleOf2(value: Int) = if (value % 2 != 0) value + 1 else value

    companion object {
        fun readFrom(blob: Blob) = readFrom(blob.toBlock())

        fun readFrom(stream: DataInput): Alias {
            val (appInfo, recSize, version) = AliasHeader.readFrom(stream)

            require(recSize >= 150) { "Incorrect alias length: $recSize" }
            require(version in 2..3) { "Unsupported alias version $version" }

            val record = if (version == 2.toShort()) {
                AliasRecordV2.readFrom(stream)
            } else {
                AliasRecordV3.readFrom(stream)
            }

            val alias = Builder(
                appInfo = appInfo,
                version = version,
                volumeInfo = record.deriveVolumeInfo(),
                targetInfo = record.deriveTargetInfo(),
            )

            var tagValue = stream.readShort()
            while (tagValue != (-1).toShort()) {
                val length = stream.readShort().toInt()
                when (Tag.findForValue(tagValue)) {
                    Tag.CARBON_FOLDER_NAME -> alias.targetInfo.folderName =
                        stream.readString(length, StandardCharsets.UTF_8).replace("/", ":")
                    Tag.CNID_PATH -> alias.targetInfo.cnidPath =
                        (0..(length / 4)).map { stream.readUInt() }
                    Tag.CARBON_PATH -> alias.targetInfo.carbonPath =
                        stream.readString(length, StandardCharsets.UTF_8)
                    Tag.APPLESHARE_ZONE -> alias.volumeInfo.lazyAppleShareInfo().zone =
                        stream.readString(length, StandardCharsets.UTF_8)
                    Tag.APPLESHARE_SERVER_NAME -> alias.volumeInfo.lazyAppleShareInfo().server =
                        stream.readString(length, StandardCharsets.UTF_8)
                    Tag.APPLESHARE_USERNAME -> alias.volumeInfo.lazyAppleShareInfo().user =
                        stream.readString(length, StandardCharsets.UTF_8)
                    Tag.DRIVER_NAME -> alias.volumeInfo.driverName =
                        stream.readString(length, StandardCharsets.UTF_8)
                    Tag.NETWORK_MOUNT_INFO -> alias.volumeInfo.networkMountInfo = stream.readBlob(length)
                    Tag.DIALUP_INFO -> alias.volumeInfo.dialupInfo = stream.readBlob(length)
                    Tag.UNICODE_FILENAME -> {
                        stream.skip(2)
                        alias.targetInfo.name = stream.readString(length - 2, StandardCharsets.UTF_16BE)
                    }
                    Tag.UNICODE_VOLUME_NAME -> {
                        stream.skip(2)
                        alias.volumeInfo.name = stream.readString(length - 2, StandardCharsets.UTF_16BE)
                    }
                    Tag.HIGH_RES_VOLUME_CREATION_DATE -> alias.volumeInfo.creationDate =
                        MacTimeUtils.decodeHighResInstant(stream.readLong())
                    Tag.HIGH_RES_CREATION_DATE -> alias.targetInfo.creationDate =
                        MacTimeUtils.decodeHighResInstant(stream.readLong())
                    Tag.POSIX_PATH -> alias.targetInfo.posixPath =
                        stream.readString(length, StandardCharsets.UTF_8)
                    Tag.POSIX_PATH_TO_MOUNTPOINT -> alias.volumeInfo.posixPath =
                        stream.readString(length, StandardCharsets.UTF_8)
                    Tag.RECURSIVE_ALIAS_OF_DISK_IMAGE -> alias.volumeInfo.diskImageAlias =
                        readFrom(stream.readBlob(length).toBlock())
                    Tag.USER_HOME_LENGTH_PREFIX -> alias.targetInfo.userHomePrefixLen = stream.readShort()
                    null -> alias.extra.add(tagValue to stream.readBlob(length))
                }

                // Pad to 2-byte boundary
                if ((length and 1) != 0) {
                    stream.skip(1)
                }

                tagValue = stream.readShort()
            }

            return alias.build()
        }
    }

    class Builder(
        val appInfo: FourCC,
        val version: Short,
        val volumeInfo: VolumeInfo.Builder,
        val targetInfo: TargetInfo.Builder,
        val extra: MutableList<Pair<Short, Blob>> = mutableListOf()
    ) {
        fun build() = Alias(appInfo, version, volumeInfo.build(), targetInfo.build(), extra)
    }
}