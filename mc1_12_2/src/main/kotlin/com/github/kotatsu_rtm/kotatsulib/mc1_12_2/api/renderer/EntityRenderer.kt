package com.github.kotatsu_rtm.kotatsulib.mc1_12_2.api.renderer

import com.github.kotatsu_rtm.kotatsulib.api.toRadians
import com.github.kotatsu_rtm.kotatsulib.mc1_12_2.api.gl.GLStateImpl
import net.minecraft.client.renderer.entity.Render
import net.minecraft.client.renderer.entity.RenderManager
import net.minecraft.entity.Entity
import net.minecraft.util.math.MathHelper
import org.joml.Matrix4f

abstract class EntityRenderer<T : Entity>(renderManager: RenderManager) : RendererBase<T>, Render<T>(renderManager) {
    @Suppress("WRONG_NULLABILITY_FOR_JAVA_OVERRIDE")
    override fun doRender(entity: T?, x: Double, y: Double, z: Double, entityYaw: Float, tickProgression: Float) {
        val posX = entity?.let { it.lastTickPosX + (it.posX - it.lastTickPosX) * tickProgression } ?: 0.0
        val posY = entity?.let { it.lastTickPosY + (it.posY - it.lastTickPosY) * tickProgression } ?: 0.0
        val posZ = entity?.let { it.lastTickPosZ + (it.posZ - it.lastTickPosZ) * tickProgression } ?: 0.0
        val pitch =
            entity?.let {
                (it.prevRotationPitch + (it.rotationPitch - it.prevRotationPitch) * tickProgression).toRadians()
            } ?: 0.0F
        val yaw =
            entity?.let {
                (
                    it.prevRotationYaw +
                        MathHelper.wrapDegrees(it.rotationYaw - it.prevRotationYaw) * tickProgression
                ).toRadians()
            } ?: 0.0F
        val modelMatrix = Matrix4f()
        modelMatrix.translate((x + posX).toFloat(), (y + posY).toFloat(), (z + posZ).toFloat())
        modelMatrix.rotateX(pitch)
        modelMatrix.rotateY(yaw)
        modelMatrix.mul(getModelOffset(entity, tickProgression))

        render(
            entity,
            tickProgression,
            modelMatrix, GLStateImpl.getModelView(), getProjectionMatrix(entity),
            getLightMapCoordinate(entity)
        )
    }

    abstract fun getModelOffset(entity: T?, tickProgression: Float): Matrix4f
}
