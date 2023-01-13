package garden.ephemeral.macfiles.dsstore.codecs

import garden.ephemeral.macfiles.common.io.ByteBufferDataOutput
import garden.ephemeral.macfiles.common.types.Blob
import garden.ephemeral.macfiles.dsstore.types.IntPoint
import java.nio.ByteBuffer

object IconLocationCodec : Codec<IntPoint> {
    override fun decode(blob: Blob): IntPoint {
        val stream = blob.toBlock()
        val x = stream.readInt()
        val y = stream.readInt()
        @Suppress("GrazieInspection")
        // Ignores the other 8 bytes which appear to always be FF FF FF FF FF FF 00 00
        return IntPoint(x, y)
    }

    override fun encode(value: IntPoint): Blob {
        val data = ByteArray(16)
        val stream = ByteBufferDataOutput(ByteBuffer.wrap(data))
        stream.writeInt(value.x)
        stream.writeInt(value.y)
        stream.writeInt(0xFFFFFFFF.toInt())
        stream.writeInt(0xFFFF0000.toInt())
        return Blob(data)
    }
}