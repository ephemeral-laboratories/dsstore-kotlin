package garden.ephemeral.macfiles.alias

import garden.ephemeral.macfiles.common.MacTimeUtils
import garden.ephemeral.macfiles.common.io.DataInput
import garden.ephemeral.macfiles.common.io.DataOutput
import garden.ephemeral.macfiles.common.types.Blob
import java.nio.charset.StandardCharsets

data class AliasRecordV3(
    val kind: Short,
    val volumeDateHighRes: Long,
    val fsType: FileSystemType,
    val diskType: Short,
    val folderCnid: UInt,
    val cnid: UInt,
    val creationDateHighRes: Long,
    val volumeAttributes: UInt,
    val reserved: Blob,
) : AliasRecord {
    override fun deriveVolumeInfo() = VolumeInfo.Builder(
        name = "",
        creationDate = MacTimeUtils.decodeHighResInstant(volumeDateHighRes),
        fsType = fsType,
        diskType = VolumeType.forValue(diskType),
        attributeFlags = volumeAttributes,
    )

    override fun deriveTargetInfo() = TargetInfo.Builder(
        kind = Kind.forValue(kind),
        name = "",
        folderCnid = folderCnid,
        cnid = cnid,
        creationDate = MacTimeUtils.decodeHighResInstant(creationDateHighRes)
    )

    override fun writeTo(stream: DataOutput) {
        stream.writeShort(kind)
        stream.writeLong(volumeDateHighRes)
        stream.writeString(4, fsType.identifier, StandardCharsets.UTF_8)
        stream.writeShort(diskType)
        stream.writeUInt(folderCnid)
        stream.writeUInt(cnid)
        stream.writeLong(creationDateHighRes)
        stream.writeUInt(volumeAttributes)
        stream.writeBlob(reserved)
    }

    companion object {
        const val SIZE = 50

        fun readFrom(stream: DataInput): AliasRecordV3 {
            val kind = stream.readShort()
            val volumeDateHighRes = stream.readLong()
            val fsType = FileSystemType.forIdentifier(stream.readString(4, StandardCharsets.UTF_8))
            val diskType = stream.readShort()
            val folderCnid = stream.readUInt()
            val cnid = stream.readUInt()
            val creationDateHighRes = stream.readLong()
            val volumeAttributes = stream.readUInt()
            val reserved = stream.readBlob(14)
            return AliasRecordV3(
                kind, volumeDateHighRes, fsType, diskType, folderCnid, cnid,
                creationDateHighRes, volumeAttributes, reserved
            )
        }

        fun forAlias(alias: Alias) = AliasRecordV3(
            kind = alias.target.kind.value,
            volumeDateHighRes = MacTimeUtils.encodeHighResInstant(alias.volume.creationDate),
            fsType = alias.volume.fsType,
            diskType = alias.volume.diskType.value,
            folderCnid = alias.target.folderCnid,
            cnid = alias.target.cnid,
            creationDateHighRes = MacTimeUtils.encodeHighResInstant(alias.target.creationDate),
            volumeAttributes = alias.volume.attributeFlags,
            reserved = Blob.zeroes(14)
        )
    }
}