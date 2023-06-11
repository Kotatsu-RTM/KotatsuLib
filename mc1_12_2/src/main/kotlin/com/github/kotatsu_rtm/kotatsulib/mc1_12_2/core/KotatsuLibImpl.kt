package com.github.kotatsu_rtm.kotatsulib.mc1_12_2.core

import com.github.kotatsu_rtm.kotatsulib.core.KotatsuLib
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.Mod.EventHandler
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import org.apache.logging.log4j.LogManager

@Mod(modid = KotatsuLib.MOD_ID, name = KotatsuLib.MOD_NAME, version = KotatsuLib.MOD_VERSION)
class KotatsuLibImpl {
    @EventHandler
    fun onFMLInit(@Suppress("UNUSED_PARAMETER") event: FMLInitializationEvent) {
        LogManager.getLogger().info("""
            Initialize KotatsuLib
            categoryForDevelopers: ${KotatsuLibConfigImpl.categoryForDevelopers}
            enableOpenGLDebugOutput: ${KotatsuLibConfigImpl.enableOpenGLDebugOutput.get().getOrThrow()}
        """.trimIndent())
    }
}
