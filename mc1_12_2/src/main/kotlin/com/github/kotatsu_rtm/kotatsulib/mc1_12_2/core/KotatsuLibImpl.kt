package com.github.kotatsu_rtm.kotatsulib.mc1_12_2.core

import com.github.kotatsu_rtm.kotatsulib.core.KotatsuLib
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.Mod.EventHandler
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import org.apache.logging.log4j.LogManager

@Suppress("unused")
@Mod(modid = KotatsuLib.MOD_ID, name = KotatsuLib.MOD_NAME, version = KotatsuLib.MOD_VERSION, guiFactory = "com.github.kotatsu_rtm.kotatsulib.mc1_12_2.core.KotatsuLibConfigImpl\$Gui")
class KotatsuLibImpl {
    @EventHandler
    fun onFMLPreInit(@Suppress("UNUSED_PARAMETER") event: FMLPreInitializationEvent) {
        KotatsuLib.updateDebugOutputState(KotatsuLibConfigImpl)
    }

    @EventHandler
    fun onFMLInit(@Suppress("UNUSED_PARAMETER") event: FMLInitializationEvent) {
        MinecraftForge.EVENT_BUS.register(KotatsuLibConfigImpl)

        LogManager.getLogger().info("""
            Initialize KotatsuLib
            categoryForDevelopers: ${KotatsuLibConfigImpl.categoryForDevelopers}
            enableOpenGLDebugOutput: ${KotatsuLibConfigImpl.enableOpenGLDebugOutput.get().getOrThrow()}
        """.trimIndent())
    }
}
