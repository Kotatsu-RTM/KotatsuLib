package com.github.kotatsu_rtm.kotatsulib.api.shader

import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL20

abstract class ShaderManager {
    protected fun flushShaders() {
        val boundTexture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D)
        val blendState = GL11.glGetBoolean(GL11.GL_BLEND)

        if (blendState) GL11.glDisable(GL11.GL_BLEND)

        SHADERS.forEach(Shader<*>::flush)

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, boundTexture)
        if (blendState) GL11.glEnable(GL11.GL_BLEND)

        GL20.glUseProgram(0)
    }

    companion object {
        private val SHADERS by lazy {
            listOf(
                TexturedShader,
                ColoredShader,
                ColoredInstancedShader
            )
        }
    }
}
