package com.github.kotatsu_rtm.kotatsulib.mc1_12_2.api.gl

import com.github.kotatsu_rtm.kotatsulib.api.gl.GLState
import net.minecraft.client.renderer.GLAllocation
import net.minecraftforge.client.event.EntityViewRenderEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.joml.Matrix4f
import org.lwjgl.opengl.GL11
import kotlin.properties.Delegates

object GLStateImpl : GLState {
    private var viewMatrix by Delegates.notNull<Matrix4f>()
    private var projectionMatrix by Delegates.notNull<Matrix4f>()
    private val matrixBuffer = GLAllocation.createDirectFloatBuffer(16)

    override fun getView() = Matrix4f(viewMatrix)

    override fun getProjection() = Matrix4f(projectionMatrix)

    @Suppress("MemberVisibilityCanBePrivate")
    fun getModelViewMatrixFromGL() =
        matrixBuffer.apply {
            rewind()
            GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, this)
            rewind()
        }.let { Matrix4f(it) }

    @Suppress("MemberVisibilityCanBePrivate")
    fun getProjectionMatrixFromGL() =
        matrixBuffer.apply {
            rewind()
            GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, this)
            rewind()
        }.let { Matrix4f(it) }

    @Suppress("unused")
    @SubscribeEvent
    fun fogDensity(@Suppress("UNUSED_PARAMETER") event: EntityViewRenderEvent.FogDensity) {
        viewMatrix = getModelViewMatrixFromGL()

        projectionMatrix = getProjectionMatrixFromGL()
    }
}
