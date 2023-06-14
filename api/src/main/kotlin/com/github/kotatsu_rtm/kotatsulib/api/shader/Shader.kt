package com.github.kotatsu_rtm.kotatsulib.api.shader

import dev.siro256.forgelib.rtm_glsl.shader.Shader
import dev.siro256.forgelib.rtm_glsl.wrapper.VertexArrayObject
import org.lwjgl.opengl.GL20

abstract class Shader<T : Shader.RenderData>(
    vertexShaderSource: String,
    fragmentShaderSource: String,
) : Shader<T>(vertexShaderSource, fragmentShaderSource) {
    protected abstract val vao: VertexArrayObject

    final override fun flush() {
        if (callBuffer.isEmpty()) return

        GL20.glUseProgram(name)
        vao.bind()
        preDraw()

        draw()

        postDraw()
        vao.unbind()
    }

    protected abstract fun draw()

    protected open fun preDraw() {}

    protected open fun postDraw() {}
}
