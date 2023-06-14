package com.github.kotatsu_rtm.kotatsulib.core

import com.github.kotatsu_rtm.kotatsulib.api.config.Config
import com.github.kotatsu_rtm.kotatsulib.api.config.ConfigType
import java.util.Optional

abstract class KotatsuLibConfig<T : Any>(configType: ConfigType<T>) : Config<T> {
    final override val relativePathFromGameDirectory = "config/${KotatsuLib.MOD_ID}.cfg"

    val categoryForDevelopers by lazy {
        configType.createCategory(
            this,
            Optional.empty(),
            "$KEY_CATEGORY_BASE.for_developers",
            Optional.of("Convenient tools for developers. If you're not a developer, you may not need this.")
        )
    }

    val enableOpenGLDebugOutput by lazy {
        configType.createBooleanProperty(
            this,
            categoryForDevelopers,
            "$KEY_PROPERTY_BASE.enable_opengl_debug_output",
            false,
            Optional.of(
                """
                Enable OpenGL debug output (known as KHR_debug).
                This is a convenient method for debugging OpenGL things.
                e.g. when you writing a new shader, picking up errors and log to the Minecraft game output.
                """.trimIndent()
            )
        )
    }

    companion object {
        private const val KEY_BASE = "${KotatsuLib.MOD_ID}.config"
        private const val KEY_CATEGORY_BASE = "$KEY_BASE.category"
        private const val KEY_PROPERTY_BASE = "$KEY_BASE.property"
    }
}
