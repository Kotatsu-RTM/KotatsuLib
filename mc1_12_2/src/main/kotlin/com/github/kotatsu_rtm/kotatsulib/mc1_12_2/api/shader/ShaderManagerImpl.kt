package com.github.kotatsu_rtm.kotatsulib.mc1_12_2.api.shader

import com.github.kotatsu_rtm.kotatsulib.api.shader.ShaderManager
import net.minecraftforge.client.event.GuiScreenEvent.DrawScreenEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@Suppress("unused")
object ShaderManagerImpl : ShaderManager() {
    @SubscribeEvent
    fun onRenderWorldLast(@Suppress("UNUSED_PARAMETER") event: RenderWorldLastEvent) {
        flushShaders()
    }

    @SubscribeEvent
    fun onPostDrawScreen(@Suppress("UNUSED_PARAMETER") event: DrawScreenEvent.Post) {
        flushShaders()
    }
}
