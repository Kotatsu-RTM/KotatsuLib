package com.github.kotatsu_rtm.kotatsulib.mc1_12_2.api.renderer

import jp.ngt.rtm.entity.npc.EntityNPC
import net.minecraft.client.renderer.entity.RenderManager
import org.joml.Matrix4f

abstract class CustomNPCRenderer<T : EntityNPC>(
    renderManager: RenderManager
) : EntityRenderer<T>(renderManager) {
    override fun getModelOffset(entity: T?, tickProgression: Float): Matrix4f {
        return Matrix4f()
    }
}
