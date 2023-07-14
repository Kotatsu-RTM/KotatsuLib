package com.github.kotatsu_rtm.kotatsulib.mc1_12_2.api.renderer

import com.github.kotatsu_rtm.kotatsulib.api.toRadians
import jp.ngt.rtm.entity.vehicle.EntityVehicleBase
import net.minecraft.client.renderer.entity.RenderManager
import org.joml.Matrix4f

abstract class CustomVehicleRenderer<T : EntityVehicleBase<*>>(
    renderManager: RenderManager
) : EntityRenderer<T>(renderManager) {
    override fun getModelOffset(entity: T?, tickProgression: Float): Matrix4f {
        val offset = Matrix4f()
        val roll =
            entity?.let {
                (it.prevRotationRoll + (it.rotationRoll - it.prevRotationRoll) * tickProgression).toRadians()
            } ?: 0.0F
        offset.rotateZ(roll)
        return offset
    }
}
