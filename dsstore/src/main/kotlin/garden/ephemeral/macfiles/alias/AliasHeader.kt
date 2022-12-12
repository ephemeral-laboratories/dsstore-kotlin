package garden.ephemeral.macfiles.alias

import garden.ephemeral.macfiles.common.io.DataInput
import garden.ephemeral.macfiles.common.types.FourCC

data class AliasHeader(
    val appInfo: FourCC,
    val recSize: Short,
    val version: Short
) {

    companion object {
        const val SIZE = 8

        fun readFrom(stream: DataInput): AliasHeader {
            val appInfo = stream.readFourCC()
            val recSize = stream.readShort()
            val version = stream.readShort()
            return AliasHeader(appInfo, recSize, version)
        }
    }
}