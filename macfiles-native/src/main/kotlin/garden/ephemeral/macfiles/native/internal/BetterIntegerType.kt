package garden.ephemeral.macfiles.native.internal

import com.sun.jna.IntegerType

/**
 * Alternative base class for integer types which also implements all the additional
 * methods which JNA's version does not.
 */
abstract class BetterIntegerType(size: Int, value: Long, unsigned: Boolean) : IntegerType(size, value, unsigned) {
    override fun toByte(): Byte {
        return toInt().toByte()
    }

    override fun toChar(): Char {
        return toInt().toChar()
    }

    override fun toShort(): Short {
        return toInt().toShort()
    }
}