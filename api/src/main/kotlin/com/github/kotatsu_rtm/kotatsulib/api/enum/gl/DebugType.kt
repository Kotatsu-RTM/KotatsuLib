package com.github.kotatsu_rtm.kotatsulib.api.enum.gl

import org.lwjgl.opengl.GL43

@Suppress("unused")
enum class DebugType(val constant: Int) {
    GL_DEBUG_TYPE_ERROR(GL43.GL_DEBUG_TYPE_ERROR),
    GL_DEBUG_TYPE_DEPRECATED_BEHAVIOR(GL43.GL_DEBUG_TYPE_DEPRECATED_BEHAVIOR),
    GL_DEBUG_TYPE_UNDEFINED_BEHAVIOR(GL43.GL_DEBUG_TYPE_UNDEFINED_BEHAVIOR),
    GL_DEBUG_TYPE_PORTABILITY(GL43.GL_DEBUG_TYPE_PORTABILITY),
    GL_DEBUG_TYPE_PERFORMANCE(GL43.GL_DEBUG_TYPE_PERFORMANCE),
    GL_DEBUG_TYPE_MARKER(GL43.GL_DEBUG_TYPE_MARKER),
    GL_DEBUG_TYPE_PUSH_GROUP(GL43.GL_DEBUG_TYPE_PUSH_GROUP),
    GL_DEBUG_TYPE_POP_GROUP(GL43.GL_DEBUG_TYPE_POP_GROUP),
    GL_DEBUG_TYPE_OTHER(GL43.GL_DEBUG_TYPE_OTHER)
}
