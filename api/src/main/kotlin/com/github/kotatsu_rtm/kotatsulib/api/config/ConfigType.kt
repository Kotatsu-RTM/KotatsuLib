package com.github.kotatsu_rtm.kotatsulib.api.config

import java.util.Optional

abstract class ConfigType<T : Any> {
    abstract fun createCategory(
        config: Config<T>,
        parent: Optional<Category>,
        key: kotlin.String,
        comment: Optional<kotlin.String>,
    ): Category

    abstract fun createBooleanProperty(
        config: Config<T>,
        parent: Category,
        key: kotlin.String,
        default: kotlin.Boolean,
        comment: Optional<kotlin.String>,
    ): Boolean

    abstract fun createIntProperty(
        config: Config<T>,
        parent: Category,
        key: kotlin.String,
        default: kotlin.Int,
        comment: Optional<kotlin.String>,
    ): Int

    abstract fun createFloatProperty(
        config: Config<T>,
        parent: Category,
        key: kotlin.String,
        default: kotlin.Float,
        comment: Optional<kotlin.String>,
    ): Float

    abstract fun createStringProperty(
        config: Config<T>,
        parent: Category,
        key: kotlin.String,
        default: kotlin.String,
        comment: Optional<kotlin.String>,
    ): String

    abstract class Base(
        val parent: Optional<Category>,
        val key: kotlin.String,
        val comment: Optional<kotlin.String>,
    ) {
        val name
            get() = toString()
        val languageKey = if (parent.isPresent) "${parent.get()}.$key" else key

        override fun toString() = if (parent.isPresent) "${parent.get()}.$key" else key.split(".").last()
    }

    abstract class HasValue<T : Any>(
        parent: Category,
        key: kotlin.String,
        @Suppress("MemberVisibilityCanBePrivate") val default: T,
        comment: Optional<kotlin.String>,
    ) : Base(Optional.of(parent), key, comment) {
        abstract fun get(): Result<T>

        abstract fun set(value: T)

        override fun toString() = "$parent.$key=${get().getOrDefault(default)}"
    }

    abstract class Category(
        parent: Optional<Category>,
        key: kotlin.String,
        comment: Optional<kotlin.String>,
    ) : Base(parent, key, comment)

    abstract class Boolean(
        parent: Category,
        key: kotlin.String,
        default: kotlin.Boolean,
        comment: Optional<kotlin.String>,
    ) : HasValue<kotlin.Boolean>(parent, key, default, comment)

    abstract class Int(
        parent: Category,
        key: kotlin.String,
        default: kotlin.Int,
        comment: Optional<kotlin.String>,
    ) : HasValue<kotlin.Int>(parent, key, default, comment)

    abstract class Float(
        parent: Category,
        key: kotlin.String,
        default: kotlin.Float,
        comment: Optional<kotlin.String>,
    ) : HasValue<kotlin.Float>(parent, key, default, comment)

    abstract class String(
        parent: Category,
        key: kotlin.String,
        default: kotlin.String,
        comment: Optional<kotlin.String>,
    ) : HasValue<kotlin.String>(parent, key, default, comment)
}
