package com.github.kotatsu_rtm.kotatsulib.api.enum.gl

import org.lwjgl.opengl.GL43

@Suppress("unused")
enum class DebugSeverity(val constant: Int) {
    GL_DEBUG_SEVERITY_HIGH(GL43.GL_DEBUG_SEVERITY_HIGH),
    GL_DEBUG_SEVERITY_MEDIUM(GL43.GL_DEBUG_SEVERITY_MEDIUM),
    GL_DEBUG_SEVERITY_LOW(GL43.GL_DEBUG_SEVERITY_LOW),
    GL_DEBUG_SEVERITY_NOTIFICATION(GL43.GL_DEBUG_SEVERITY_NOTIFICATION)
}
