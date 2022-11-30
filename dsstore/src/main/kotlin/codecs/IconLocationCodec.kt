package codecs

import types.IntPoint
import util.DataInput
import util.DataOutput

object IconLocationCodec : Codec<IntPoint> {
    override fun decode(stream: DataInput): IntPoint {
        val x = stream.readInt()
        val y = stream.readInt()
        @Suppress("GrazieInspection")
        // Ignores the other 8 bytes which appear to always be FF FF FF FF FF FF 00 00
        return IntPoint(x, y)
    }

    override fun calculateSize(value: IntPoint): Int {
        return 16
    }

    override fun encode(value: IntPoint, stream: DataOutput) {
        stream.writeInt(value.x)
        stream.writeInt(value.y)
        stream.writeInt(0xFFFFFFFF.toInt())
        stream.writeInt(0xFFFF0000.toInt())
    }
}