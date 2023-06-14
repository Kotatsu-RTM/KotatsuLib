package com.github.kotatsu_rtm.kotatsulib.api.kotlinutil

import java.util.Optional
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class InvokeBlockOnChange<T>(private val block: (T) -> Unit) : ReadWriteProperty<Any?, T> {
    private var _value = Optional.empty<T>()

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        if (_value.isPresent) return _value.get()
        throw IllegalStateException("Value must be initialized before get")
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        if (_value.isPresent && _value.get() == value) return
        _value = Optional.ofNullable(value)
        block.invoke(value)
    }
}
