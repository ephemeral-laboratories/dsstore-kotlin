package codecs

import types.IntPoint
import util.DataInput

object IconLocationCodec : Codec<IntPoint> {
    override fun decode(stream: DataInput): IntPoint {
        val x = stream.readInt()
        val y = stream.readInt()
        // Ignores the other 8 bytes which appear to always be FF FF FF FF FF FF 00 00
        return IntPoint(x, y)
    }
}