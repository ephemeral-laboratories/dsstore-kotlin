package garden.ephemeral.macfiles.native.internal

import com.sun.jna.Pointer
import com.sun.jna.Structure

// Definitions found in `MacTypes.h`


/**
 * ```c
 * typedef unsigned int FourCharCode;
 * ```
 */
internal class FourCharCode(value: Int = 0) : BetterIntegerType(SIZE, value.toLong(), true) {
    companion object {
        const val SIZE = 4
    }
}

/**
 * ```c
 * typedef FourCharCode OSType;
 * ```
 */
internal typealias OSType = FourCharCode

/**
 * ```c
 * struct Point {
 *     short               v;
 *     short               h;
 * };
 * typedef struct Point                    Point;
 * typedef Point *                         PointPtr;
 * ```
 */
@Structure.FieldOrder("v", "h")
class Point(p: Pointer? = null) : Structure(p) {
    @JvmField
    var v: Short = 0

    @JvmField
    var h: Short = 0

    init {
        if (p != null) {
            read()
        }
    }

    companion object {
        const val SIZE = 2 + 2
    }
}

/**
 * ```c
 * struct Rect {
 *     short               top;
 *     short               left;
 *     short               bottom;
 *     short               right;
 * };
 * typedef struct Rect                     Rect;
 * typedef Rect *                          RectPtr;
 */
@Structure.FieldOrder("top", "left", "bottom", "right")
class Rect(p: Pointer? = null) : Structure(p) {
    @JvmField
    var top: Short = 0

    @JvmField
    var left: Short = 0

    @JvmField
    var bottom: Short = 0

    @JvmField
    var right: Short = 0

    init {
        if (p != null) {
            read()
        }
    }

    companion object {
        const val SIZE = 2 + 2 + 2 + 2
    }
}
