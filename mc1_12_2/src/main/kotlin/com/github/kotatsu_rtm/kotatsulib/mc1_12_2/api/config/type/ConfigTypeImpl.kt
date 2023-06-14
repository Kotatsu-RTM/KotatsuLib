package com.github.kotatsu_rtm.kotatsulib.mc1_12_2.api.config.type

import com.github.kotatsu_rtm.kotatsulib.api.config.Config
import com.github.kotatsu_rtm.kotatsulib.api.config.ConfigType
import net.minecraftforge.common.config.ConfigCategory
import net.minecraftforge.common.config.Configuration
import net.minecraftforge.common.config.Property
import java.util.*
import kotlin.jvm.optionals.getOrDefault
import kotlin.jvm.optionals.getOrNull

object ConfigTypeImpl : ConfigType<Configuration>() {
    override fun createCategory(
        config: Config<Configuration>,
        parent: Optional<ConfigType.Category>,
        key: kotlin.String,
        comment: Optional<kotlin.String>,
    ) = Category(config, parent, key, comment)

    override fun createBooleanProperty(
        config: Config<Configuration>,
        parent: ConfigType.Category,
        key: kotlin.String,
        default: kotlin.Boolean,
        comment: Optional<kotlin.String>,
    ) = Boolean(config, parent, key, default, comment)

    override fun createIntProperty(
        config: Config<Configuration>,
        parent: ConfigType.Category,
        key: kotlin.String,
        default: kotlin.Int,
        comment: Optional<kotlin.String>,
    ) = Int(config, parent, key, default, comment)

    override fun createFloatProperty(
        config: Config<Configuration>,
        parent: ConfigType.Category,
        key: kotlin.String,
        default: kotlin.Float,
        comment: Optional<kotlin.String>,
    ) = Float(config, parent, key, default, comment)

    override fun createStringProperty(
        config: Config<Configuration>,
        parent: ConfigType.Category,
        key: kotlin.String,
        default: kotlin.String,
        comment: Optional<kotlin.String>,
    ) = String(config, parent, key, default, comment)

    class Category(
        private val config: Config<Configuration>,
        parent: Optional<ConfigType.Category>,
        key: kotlin.String,
        comment: Optional<kotlin.String>,
    ) : ConfigType.Category(parent, key, comment) {
        @Suppress("unused")
        val nativeInstance: ConfigCategory =
            config.nativeInstance.getCategory(name)
                .apply {
                    setLanguageKey(this@Category.languageKey)
                    comment.ifPresent { setComment(it) }
                    config.nativeInstance.save()
                }
    }

    class Boolean(
        private val config: Config<Configuration>,
        parent: ConfigType.Category,
        key: kotlin.String,
        default: kotlin.Boolean,
        comment: Optional<kotlin.String>,
    ) : ConfigType.Boolean(parent, key, default, comment) {
        @Suppress("MemberVisibilityCanBePrivate")
        val nativeInstance: Property =
            config.nativeInstance.get(parent.name, key.split(".").last(), default, comment.getOrNull())
                .apply {
                    languageKey = this@Boolean.languageKey
                    setComment("${comment.getOrDefault("")} [default: $default]")
                    config.nativeInstance.save()
                }

        override fun get() = Result.success(nativeInstance.boolean)

        override fun set(value: kotlin.Boolean) {
            nativeInstance.set(value)
            config.nativeInstance.save()
        }
    }

    class Int(
        private val config: Config<Configuration>,
        parent: ConfigType.Category,
        key: kotlin.String,
        default: kotlin.Int,
        comment: Optional<kotlin.String>,
    ) : ConfigType.Int(parent, key, default, comment) {
        @Suppress("MemberVisibilityCanBePrivate")
        val nativeInstance: Property =
            config.nativeInstance.get(parent.name, key.split(".").last(), default, comment.getOrNull())
                .apply {
                    languageKey = this@Int.languageKey
                    setComment("${comment.getOrDefault("")} [default: $default]")
                    config.nativeInstance.save()
                }

        override fun get() = Result.success(nativeInstance.int)

        override fun set(value: kotlin.Int) {
            nativeInstance.set(value)
            config.nativeInstance.save()
        }
    }

    class Float(
        private val config: Config<Configuration>,
        parent: ConfigType.Category,
        key: kotlin.String,
        default: kotlin.Float,
        comment: Optional<kotlin.String>,
    ) : ConfigType.Float(parent, key, default, comment) {
        @Suppress("MemberVisibilityCanBePrivate")
        val nativeInstance: Property =
            config.nativeInstance.get(parent.name, key.split(".").last(), default.toDouble(), comment.getOrNull())
                .apply {
                    languageKey = this@Float.languageKey
                    setComment("${comment.getOrDefault("")} [default: $default]")
                    config.nativeInstance.save()
                }

        override fun get(): Result<kotlin.Float> = Result.success(nativeInstance.double.toFloat())

        override fun set(value: kotlin.Float) {
            nativeInstance.set(value.toDouble())
            config.nativeInstance.save()
        }
    }

    class String(
        private val config: Config<Configuration>,
        parent: ConfigType.Category,
        key: kotlin.String,
        default: kotlin.String,
        comment: Optional<kotlin.String>,
    ) : ConfigType.String(parent, key, default, comment) {
        @Suppress("MemberVisibilityCanBePrivate")
        val nativeInstance: Property =
            config.nativeInstance.get(parent.name, key.split(".").last(), default, comment.getOrNull())
                .apply {
                    languageKey = this@String.languageKey
                    setComment("${comment.getOrDefault("")} [default: $default]")
                    config.nativeInstance.save()
                }

        override fun get(): Result<kotlin.String> = Result.success(nativeInstance.string)

        override fun set(value: kotlin.String) {
            nativeInstance.set(value)
            config.nativeInstance.save()
        }
    }
}
