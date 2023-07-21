package com.github.kotatsu_rtm.kotatsulib.mc1_12_2.api.gl

import com.github.kotatsu_rtm.kotatsulib.api.gl.GLState
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GLAllocation
import net.minecraft.client.renderer.entity.RenderManager
import org.joml.Matrix4f
import org.joml.Vector3f
import org.lwjgl.opengl.GL11
import kotlin.properties.Delegates

object GLStateImpl : GLState {
    private var lastFrameIndexView = -1
    private var lastFrameIndexProjection = -1
    private var viewMatrix by Delegates.notNull<Matrix4f>()
    private var projectionMatrix by Delegates.notNull<Matrix4f>()
    private val matrixBuffer = GLAllocation.createDirectFloatBuffer(16)

    override fun getView(): Matrix4f {
        val currentFrameIndex = Minecraft.getMinecraft().frameTimer.index

        if (lastFrameIndexView != currentFrameIndex) {
            lastFrameIndexView = currentFrameIndex

            val renderPosX =
                RenderManager::class.java
                    .getDeclaredField("field_78725_b")
                    .apply { isAccessible = true }
                    .get(Minecraft.getMinecraft().renderManager) as Double
            val renderPosY =
                RenderManager::class.java
                    .getDeclaredField("field_78726_c")
                    .apply { isAccessible = true }
                    .get(Minecraft.getMinecraft().renderManager) as Double
            val renderPosZ =
                RenderManager::class.java
                    .getDeclaredField("field_78723_d")
                    .apply { isAccessible = true }
                    .get(Minecraft.getMinecraft().renderManager) as Double
            val renderPos = Vector3f(-renderPosX.toFloat(), -renderPosY.toFloat(), -renderPosZ.toFloat())

            viewMatrix =
                matrixBuffer.apply {
                    rewind()
                    GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, this)
                    rewind()
                }.let { Matrix4f(it).translate(renderPos) }
        }

        return viewMatrix
    }

    override fun getProjection(): Matrix4f {
        val currentFrameIndex = Minecraft.getMinecraft().frameTimer.index

        if (lastFrameIndexProjection != currentFrameIndex) {
            lastFrameIndexProjection = currentFrameIndex

            projectionMatrix =
                matrixBuffer.apply {
                    rewind()
                    GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, this)
                    rewind()
                }.let { Matrix4f(it) }
        }

        return projectionMatrix
    }
}
