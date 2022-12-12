package garden.ephemeral.macfiles.alias

import garden.ephemeral.macfiles.common.MacTimeUtils
import garden.ephemeral.macfiles.common.io.DataInput
import garden.ephemeral.macfiles.common.types.Blob
import java.nio.charset.StandardCharsets

data class AliasRecordV3(
    val kind: Short,
    val volumeDateHighRes: Long,
    val fsType: String,
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

    companion object {
        fun readFrom(stream: DataInput): AliasRecordV3 {
            val kind = stream.readShort()
            val volumeDateHighRes = stream.readLong()
            val fsType = stream.readString(4, StandardCharsets.UTF_8)
            val diskType = stream.readShort()
            val folderCnid = stream.readInt().toUInt()
            val cnid = stream.readInt().toUInt()
            val creationDateHighRes = stream.readLong()
            val volumeAttributes = stream.readInt().toUInt()
            val reserved = stream.readBlob(14)
            return AliasRecordV3(
                kind, volumeDateHighRes, fsType, diskType, folderCnid, cnid,
                creationDateHighRes, volumeAttributes, reserved
            )
        }
    }
}