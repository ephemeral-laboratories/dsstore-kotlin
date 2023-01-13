package garden.ephemeral.macfiles.dsstore.util

import kotlin.reflect.KProperty

interface GenericMutableProperty<T> : GenericProperty<T> {
    operator fun setValue(receiver: Any, property: KProperty<*>, value: T?)
}
