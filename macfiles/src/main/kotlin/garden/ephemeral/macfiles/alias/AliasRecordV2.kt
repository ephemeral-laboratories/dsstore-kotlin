package garden.ephemeral.macfiles.alias

import garden.ephemeral.macfiles.common.MacTimeUtils
import garden.ephemeral.macfiles.common.io.DataInput
import garden.ephemeral.macfiles.common.io.DataOutput
import garden.ephemeral.macfiles.common.types.Blob
import garden.ephemeral.macfiles.common.types.FourCC
import java.nio.charset.StandardCharsets

class AliasRecordV2(
    val kind: Short,
    val volName: String,
    val volDate: UInt,
    val fsType: FileSystemType,
    val diskType: Short,
    val folderCnid: UInt,
    val filename: String,
    val cnid: UInt,
    val creationDate: UInt,
    val creatorCode: FourCC?,
    val typeCode: FourCC?,
    val levelsFrom: Short,
    val levelsTo: Short,
    val volumeAttributes: UInt,
    val volFsId: String?,
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
        name = filename.replace("/", ":"),
        folderCnid = folderCnid,
        cnid = cnid,
        creationDate = MacTimeUtils.decodeLowResInstant(creationDate),
        creatorCode = creatorCode,
        typeCode = typeCode
    )

    override fun writeTo(stream: DataOutput) {
        stream.writeShort(kind)
        stream.writePascalString(28, volName, StandardCharsets.UTF_8)
        stream.writeUInt(volDate)
        stream.writeString(2, fsType.identifier, StandardCharsets.UTF_8)
        stream.writeShort(diskType)
        stream.writeUInt(folderCnid)
        stream.writePascalString(64, filename, StandardCharsets.UTF_8)
        stream.writeUInt(cnid)
        stream.writeUInt(creationDate)
        stream.writeFourCC(creatorCode ?: FourCC.ZERO)
        stream.writeFourCC(typeCode ?: FourCC.ZERO)
        stream.writeShort(levelsFrom)
        stream.writeShort(levelsTo)
        stream.writeUInt(volumeAttributes)
        stream.writeString(2, volFsId ?: "", StandardCharsets.UTF_8)
        stream.writeBlob(reserved)
    }

    companion object {
        const val SIZE = 142

        fun readFrom(stream: DataInput): AliasRecordV2 {
            val kind = stream.readShort()
            val volName = stream.readPascalString(28, StandardCharsets.UTF_8)
            val volDate = stream.readUInt()
            val fsType = FileSystemType.forIdentifier(stream.readString(2, StandardCharsets.UTF_8))
            val diskType = stream.readShort()
            val folderCnid = stream.readUInt()
            val filename = stream.readPascalString(64, StandardCharsets.UTF_8)
            val cnid = stream.readUInt()
            val creationDate = stream.readUInt()
            val creatorCode = stream.readFourCC()
            val typeCode = stream.readFourCC()
            val levelsFrom = stream.readShort()
            val levelsTo = stream.readShort()
            val volAttrs = stream.readUInt()
            val volFsId = stream.readString(2, StandardCharsets.UTF_8)
            val reserved = stream.readBlob(10)
            return AliasRecordV2(
                kind, volName, volDate, fsType, diskType, folderCnid, filename, cnid,
                creationDate, creatorCode, typeCode, levelsFrom, levelsTo, volAttrs, volFsId, reserved
            )
        }

        fun forAlias(alias: Alias) = AliasRecordV2(
            kind = alias.target.kind.value,
            volName = alias.volume.name.replace(':', '/'),
            volDate = MacTimeUtils.encodeLowResInstant(alias.volume.creationDate),
            fsType = alias.volume.fsType,
            diskType = alias.volume.diskType.value,
            folderCnid = alias.target.folderCnid,
            filename = alias.target.name.replace(':', '/'),
            cnid = alias.target.cnid,
            creationDate = MacTimeUtils.encodeLowResInstant(alias.target.creationDate),
            creatorCode = alias.target.creatorCode,
            typeCode = alias.target.typeCode,
            levelsFrom = alias.target.levelsFrom,
            levelsTo = alias.target.levelsTo,
            volumeAttributes = alias.volume.attributeFlags,
            volFsId = alias.volume.fsId,
            reserved = Blob.zeroes(10)
        )
    }
}