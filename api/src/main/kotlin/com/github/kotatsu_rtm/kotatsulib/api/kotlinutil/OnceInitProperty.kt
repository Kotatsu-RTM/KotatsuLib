package com.github.kotatsu_rtm.kotatsulib.api.kotlinutil

import java.util.*
import kotlin.IllegalStateException
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class OnceInitProperty<T : Any> : ReadWriteProperty<Any, T> {
    private var _value = Optional.empty<T>()

    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        if (_value.isPresent) return _value.get()
        throw IllegalStateException("Value must be initialized before get")
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        if (_value.isPresent) throw IllegalStateException("Value is already initialized")
        _value = Optional.of(value)
    }
}
