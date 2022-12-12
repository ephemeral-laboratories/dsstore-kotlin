package garden.ephemeral.macfiles.alias

import garden.ephemeral.macfiles.common.MacTimeUtils
import garden.ephemeral.macfiles.common.io.DataInput
import garden.ephemeral.macfiles.common.types.Blob
import garden.ephemeral.macfiles.common.types.FourCC
import java.nio.charset.StandardCharsets

class AliasRecordV2(
    val kind: Short,
    val volName: String,
    val volDate: UInt,
    val fsType: String,
    val diskType: Short,
    val folderCnid: UInt,
    val filename: String,
    val cnid: UInt,
    val creationDate: UInt,
    val creatorCode: FourCC,
    val typeCode: FourCC,
    val levelsFrom: Short,
    val levelsTo: Short,
    val volumeAttributes: UInt,
    val volFsId: String,
    val reserved: Blob,
) : AliasRecord {
    override fun deriveVolumeInfo() = VolumeInfo.Builder(
        name = "",
        creationDate = MacTimeUtils.decodeLowResInstant(volDate),
        fsType = fsType,
        diskType = VolumeType.forValue(diskType),
        attributeFlags = volumeAttributes,
        fsId = volFsId,
    )

    override fun deriveTargetInfo() = TargetInfo.Builder(
        kind = Kind.forValue(kind),
        filename = filename.replace("/", ":"),
        folderCnid = folderCnid,
        cnid = cnid,
        creationDate = MacTimeUtils.decodeLowResInstant(creationDate),
        creatorCode = creatorCode,
        typeCode = typeCode
    )

    companion object {
        fun readFrom(stream: DataInput): AliasRecordV2 {
            val kind = stream.readShort()
            val volName = stream.readPascalString(28, StandardCharsets.UTF_8)
            val volDate = stream.readInt().toUInt()
            val fsType = stream.readString(2, StandardCharsets.UTF_8)
            val diskType = stream.readShort()
            val folderCnid = stream.readInt().toUInt()
            val filename = stream.readPascalString(64, StandardCharsets.UTF_8)
            val cnid = stream.readInt().toUInt()
            val creationDate = stream.readInt().toUInt()
            val creatorCode = stream.readFourCC()
            val typeCode = stream.readFourCC()
            val levelsFrom = stream.readShort()
            val levelsTo = stream.readShort()
            val volAttrs = stream.readInt().toUInt()
            val volFsId = stream.readString(2, StandardCharsets.UTF_8)
            val reserved = stream.readBlob(10)
            return AliasRecordV2(
                kind, volName, volDate, fsType, diskType, folderCnid, filename, cnid,
                creationDate, creatorCode, typeCode, levelsFrom, levelsTo, volAttrs, volFsId, reserved
            )
        }
    }
}