package garden.ephemeral.macfiles.native.internal

import com.sun.jna.Native

// BEWARE: Some types in here may actually be platform-specific, so we might find them
//         breaking as macOS lands on new architectures. They can be upgraded to detect the
//         appropriate size dynamically at a later date.

/**
 * ```c
 * typedef unsigned long		__darwin_size_t;	/* sizeof() */
 * typedef __darwin_size_t        size_t;
 */
internal class Size_t(value: Long = 0) : BetterIntegerType(SIZE, value, true) {
    companion object {
        val SIZE = Native.SIZE_T_SIZE
    }
}

/**
 * ```c
 * typedef long                    __darwin_time_t;        /* time() */
 * typedef __darwin_time_t         time_t;
 */
internal class Time_t(value: Long = 0) : BetterIntegerType(SIZE, value, false) {
    companion object {
        const val SIZE = 8
    }
}

/**
 * ```c
 * typedef __uint32_t	__darwin_uid_t;		/* [???] user IDs */
 * typedef __darwin_uid_t uid_t;
 * ```
 */
internal class Uid_t(value: Int = 0) : BetterIntegerType(SIZE, value.toLong(), true) {
    companion object {
        const val SIZE = 4
    }
}

/**
 * ```c
 * typedef u_int32_t attrgroup_t;
 * ```
 */
internal class Attrgroup_t(value: Int = 0) : BetterIntegerType(SIZE, value.toLong(), true) {
    companion object {
        const val SIZE = 4

        /**
         * Convenience method to union multiple values and get the right type back.
         *
         * @param values the values to union.
         * @return the union.
         */
        fun unionOf(vararg values: Attrgroup_t): Attrgroup_t {
            var union = 0
            for (value in values) {
                union = union.or(value.toInt())
            }
            return Attrgroup_t(union)
        }
    }
}

/**
 * ```c
 * typedef u_int32_t fsobj_type_t
 * ```
 */
internal class Fsobj_type_t(value: Int = 0) : BetterIntegerType(SIZE, value.toLong(), true) {
    companion object {
        const val SIZE = 4

        // VNode types
        const val VNON = 0
        const val VREG = 1
        const val VDIR = 2
        const val VBLK = 3
        const val VCHR = 4
        const val VLNK = 5
        const val VSOCK = 6
        const val VFIFO = 7
        const val VBAD = 8
        const val VSTR = 9
        const val VCPLX = 10
    }
}
