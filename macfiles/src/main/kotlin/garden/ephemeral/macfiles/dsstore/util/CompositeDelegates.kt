package garden.ephemeral.macfiles.dsstore.util

import garden.ephemeral.macfiles.dsstore.types.DoublePoint
import garden.ephemeral.macfiles.dsstore.types.DoubleRgbColor
import garden.ephemeral.macfiles.dsstore.types.IntPoint
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0

object CompositeDelegates {
    fun doubleRgbColor(
        r: KProperty0<Double?>,
        g: KProperty0<Double?>,
        b: KProperty0<Double?>
    ): GenericProperty<DoubleRgbColor> {
        return DoubleRgbColorProperty(r, g, b)
    }

    fun doubleRgbColor(
        r: KMutableProperty0<Double?>,
        g: KMutableProperty0<Double?>,
        b: KMutableProperty0<Double?>
    ): GenericMutableProperty<DoubleRgbColor> {
        return MutableDoubleRgbColorProperty(r, g, b)
    }

    private open class DoubleRgbColorProperty(
        open val r: KProperty0<Double?>,
        open val g: KProperty0<Double?>,
        open val b: KProperty0<Double?>
    ) : GenericProperty<DoubleRgbColor> {
        override fun getValue(receiver: Any, property: KProperty<*>): DoubleRgbColor? {
            val r = r.get() ?: return null
            val g = g.get() ?: return null
            val b = b.get() ?: return null
            return DoubleRgbColor(r, g, b)
        }
    }

    private class MutableDoubleRgbColorProperty(
        override val r: KMutableProperty0<Double?>,
        override val g: KMutableProperty0<Double?>,
        override val b: KMutableProperty0<Double?>
    ) : DoubleRgbColorProperty(r, g, b), GenericMutableProperty<DoubleRgbColor> {
        override fun setValue(receiver: Any, property: KProperty<*>, value: DoubleRgbColor?) {
            r.set(value?.r)
            g.set(value?.g)
            b.set(value?.b)
        }
    }

    fun intPoint(
        x: KProperty0<Int?>,
        y: KProperty0<Int?>
    ): GenericProperty<IntPoint> {
        return IntPointProperty(x, y)
    }

    fun intPoint(
        x: KMutableProperty0<Int?>,
        y: KMutableProperty0<Int?>
    ): GenericMutableProperty<IntPoint> {
        return MutableIntPointProperty(x, y)
    }

    private open class IntPointProperty(
        open val x: KProperty0<Int?>,
        open val y: KProperty0<Int?>
    ) : GenericProperty<IntPoint> {
        override fun getValue(receiver: Any, property: KProperty<*>): IntPoint? {
            val x = x.get() ?: return null
            val y = y.get() ?: return null
            return IntPoint(x, y)
        }
    }

    private class MutableIntPointProperty(
        override val x: KMutableProperty0<Int?>,
        override val y: KMutableProperty0<Int?>
    ) : IntPointProperty(x, y), GenericMutableProperty<IntPoint> {
        override fun setValue(receiver: Any, property: KProperty<*>, value: IntPoint?) {
            x.set(value?.x)
            y.set(value?.y)
        }
    }

    fun doublePoint(
        x: KProperty0<Double?>,
        y: KProperty0<Double?>
    ): GenericProperty<DoublePoint> {
        return DoublePointProperty(x, y)
    }

    fun doublePoint(
        x: KMutableProperty0<Double?>,
        y: KMutableProperty0<Double?>
    ): GenericMutableProperty<DoublePoint> {
        return MutableDoublePointProperty(x, y)
    }

    private open class DoublePointProperty(
        open val x: KProperty0<Double?>,
        open val y: KProperty0<Double?>
    ) : GenericProperty<DoublePoint> {
        override fun getValue(receiver: Any, property: KProperty<*>): DoublePoint? {
            val x = x.get() ?: return null
            val y = y.get() ?: return null
            return DoublePoint(x, y)
        }
    }

    private class MutableDoublePointProperty(
        override val x: KMutableProperty0<Double?>,
        override val y: KMutableProperty0<Double?>
    ) : DoublePointProperty(x, y), GenericMutableProperty<DoublePoint> {
        override fun setValue(receiver: Any, property: KProperty<*>, value: DoublePoint?) {
            x.set(value?.x)
            y.set(value?.y)
        }
    }
}
