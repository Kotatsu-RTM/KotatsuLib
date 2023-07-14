package com.github.kotatsu_rtm.kotatsulib.mc1_12_2.api.renderer

import com.github.kotatsu_rtm.kotatsulib.api.renderer.Renderer
import com.github.kotatsu_rtm.kotatsulib.mc1_12_2.api.gl.GLStateImpl
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.OpenGlHelper
import org.joml.Matrix4f
import org.joml.Vector2f

interface RendererBase<T : Any> : Renderer<T> {
    fun getProjectionMatrix(target: T?): Matrix4f =
        if (target != null) {
            GLStateImpl.getProjection()
        } else {
            val minecraft = Minecraft.getMinecraft()
            Matrix4f().perspective(80.0F, minecraft.displayWidth / minecraft.displayHeight.toFloat(), 0.1F, 500.0F)
        }

    fun getLightMapCoordinate(target: T?) =
        if (target == null) {
            MAX_BRIGHTNESS_COORDINATE
        } else {
            Vector2f((OpenGlHelper.lastBrightnessX + 8.0F) / 256.0F, (OpenGlHelper.lastBrightnessY + 8.0F) / 256.0F)
        }

    companion object {
        private val MAX_BRIGHTNESS_COORDINATE = Vector2f(248.0F / 256.0F, 248.0F / 256.0F)
    }
}
