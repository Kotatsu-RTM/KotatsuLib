package com.github.kotatsu_rtm.kotatsulib.mc1_12_2.core

import com.github.kotatsu_rtm.kotatsulib.core.KotatsuLib
import com.github.kotatsu_rtm.kotatsulib.core.KotatsuLibConfig
import com.github.kotatsu_rtm.kotatsulib.mc1_12_2.api.config.type.ConfigTypeImpl
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import net.minecraftforge.common.config.ConfigElement
import net.minecraftforge.common.config.Configuration
import net.minecraftforge.fml.client.IModGuiFactory
import net.minecraftforge.fml.client.config.GuiConfig
import net.minecraftforge.fml.client.event.ConfigChangedEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.io.File

internal object KotatsuLibConfigImpl : KotatsuLibConfig<Configuration>(ConfigTypeImpl) {
    override val nativeInstance = Configuration(File(relativePathFromGameDirectory))

    @SubscribeEvent
    fun onConfigChanged(event: ConfigChangedEvent.OnConfigChangedEvent) {
        if (event.modID != KotatsuLib.MOD_ID) return
        nativeInstance.save()
        KotatsuLib.updateDebugOutputState(KotatsuLibConfigImpl)
    }

    class Gui : IModGuiFactory {
        override fun initialize(minecraftInstance: Minecraft?) {
            //Do nothing
        }

        override fun hasConfigGui() = true

        override fun createConfigGui(parentScreen: GuiScreen) =
            GuiConfig(
                parentScreen,
                ConfigElement(nativeInstance.getCategory(categoryForDevelopers.name)).childElements,
                KotatsuLib.MOD_ID,
                false,
                false,
                relativePathFromGameDirectory
            )

        override fun runtimeGuiCategories(): Set<IModGuiFactory.RuntimeOptionCategoryElement> = setOf()
    }
}
