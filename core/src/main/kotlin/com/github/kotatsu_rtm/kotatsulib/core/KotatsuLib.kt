package com.github.kotatsu_rtm.kotatsulib.core

import com.github.kotatsu_rtm.kotatsulib.api.enum.gl.DebugSeverity
import com.github.kotatsu_rtm.kotatsulib.api.enum.gl.DebugSource
import com.github.kotatsu_rtm.kotatsulib.api.enum.gl.DebugType
import org.apache.logging.log4j.LogManager
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL43
import org.lwjgl.opengl.KHRDebugCallback

object KotatsuLib {
    const val MOD_ID = "@modId@"
    const val MOD_NAME = "@modName@"
    const val MOD_VERSION = "@modVersion@"

    val logger = LogManager.getLogger(MOD_NAME)

    fun updateDebugOutputState(config: KotatsuLibConfig<*>) {
        if (!config.enableOpenGLDebugOutput.get().getOrDefault(config.enableOpenGLDebugOutput.default)) {
            GL11.glDisable(GL43.GL_DEBUG_OUTPUT)
            return
        }

        GL11.glEnable(GL43.GL_DEBUG_OUTPUT)
        GL43.glDebugMessageControl(GL11.GL_DONT_CARE, GL11.GL_DONT_CARE, GL11.GL_DONT_CARE, null, true)
        GL43.glDebugMessageCallback(
            KHRDebugCallback { source, type, id, severity, message ->
                LogManager.getLogger().debug("""
                        KHRDebugCallBack:
                        source: ${DebugSource.values().first { it.constant == source }}
                        type: ${DebugType.values().first { it.constant == type }}
                        id: $id
                        severity: ${DebugSeverity.values().first { it.constant == severity }}
                        message: $message
                    """.trimIndent())
            }
        )
    }
}
