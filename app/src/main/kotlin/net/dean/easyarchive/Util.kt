package net.dean.easyarchive

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Lets a variable be set once to a non-null value in a non-traditional location, hence a "tardy" assignment. This
 * delegate is most useful when used with `onCreate` or similar methods. This delegate does not allow null values or
 * reassignment.
 */
public class TardyNotNullVal<T: Any>() : ReadWriteProperty<Any?, T> {
    private var value: T? = null
    private var set = false

    public override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return value ?: throw IllegalStateException("Property ${property.name} should be initialized before get.")
    }

    public override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        if (set)
            throw IllegalStateException("Property ${property.name} has already been set.")
        this.value = value
        set = true
    }
}

