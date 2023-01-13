package garden.ephemeral.macfiles.dsstore.util

import kotlin.reflect.KProperty

interface GenericProperty<T> {
    operator fun getValue(receiver: Any, property: KProperty<*>): T?
}
