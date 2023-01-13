package garden.ephemeral.macfiles.dsstore.util

import com.dd.plist.NSData
import com.dd.plist.NSDictionary
import com.dd.plist.NSNumber
import com.dd.plist.NSString
import garden.ephemeral.macfiles.alias.Alias
import garden.ephemeral.macfiles.common.types.Blob
import kotlin.reflect.KProperty

object DictionaryDelegates {
    fun booleanFrom(dictionary: NSDictionary, keyOverride: String? = null): GenericMutableProperty<Boolean> {
        return object : GenericMutableProperty<Boolean> {
            override fun getValue(receiver: Any, property: KProperty<*>): Boolean? {
                val number = dictionary[keyOverride ?: property.name] as NSNumber? ?: return null
                return number.boolValue()
            }

            override fun setValue(receiver: Any, property: KProperty<*>, value: Boolean?) {
                if (value == null) {
                    dictionary.remove(property.name)
                    return
                }
                dictionary[property.name] = NSNumber(value)
            }
        }
    }

    fun intFrom(dictionary: NSDictionary, keyOverride: String? = null): GenericMutableProperty<Int> {
        return object : GenericMutableProperty<Int> {
            override fun getValue(receiver: Any, property: KProperty<*>): Int? {
                val number = dictionary[keyOverride ?: property.name] as NSNumber? ?: return null
                return number.intValue()
            }

            override fun setValue(receiver: Any, property: KProperty<*>, value: Int?) {
                if (value == null) {
                    dictionary.remove(property.name)
                    return
                }
                dictionary[property.name] = NSNumber(value)
            }
        }
    }

    fun doubleFrom(dictionary: NSDictionary, keyOverride: String? = null): GenericMutableProperty<Double> {
        return object : GenericMutableProperty<Double> {
            override fun getValue(receiver: Any, property: KProperty<*>): Double? {
                val number = dictionary[keyOverride ?: property.name] as NSNumber? ?: return null
                return number.doubleValue()
            }

            override fun setValue(receiver: Any, property: KProperty<*>, value: Double?) {
                if (value == null) {
                    dictionary.remove(property.name)
                    return
                }
                dictionary[property.name] = NSNumber(value)
            }
        }
    }

    fun stringFrom(dictionary: NSDictionary, keyOverride: String? = null): GenericMutableProperty<String> {
        return object : GenericMutableProperty<String> {
            override fun getValue(receiver: Any, property: KProperty<*>): String? {
                val string = dictionary[keyOverride ?: property.name] as NSString? ?: return null
                return string.content
            }

            override fun setValue(receiver: Any, property: KProperty<*>, value: String?) {
                if (value == null) {
                    dictionary.remove(property.name)
                    return
                }
                dictionary[property.name] = NSString(value)
            }
        }
    }

    fun aliasFrom(dictionary: NSDictionary): GenericMutableProperty<Alias> {
        return object : GenericMutableProperty<Alias> {
            override fun getValue(receiver: Any, property: KProperty<*>): Alias? {
                val data = dictionary[property.name] as NSData? ?: return null
                val blob = Blob(data.bytes())
                return Alias.readFrom(blob)
            }

            override fun setValue(receiver: Any, property: KProperty<*>, value: Alias?) {
                if (value == null) {
                    dictionary.remove(property.name)
                    return
                }
                val blob = value.toBlob()
                dictionary[property.name] = NSData(blob.toByteArray())
            }
        }
    }
}