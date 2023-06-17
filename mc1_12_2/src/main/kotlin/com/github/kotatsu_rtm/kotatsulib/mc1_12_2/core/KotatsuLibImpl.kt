package com.github.kotatsu_rtm.kotatsulib.mc1_12_2.core

import com.github.kotatsu_rtm.kotatsulib.core.KotatsuLib
import com.github.kotatsu_rtm.kotatsulib.mc1_12_2.api.shader.ShaderManagerImpl
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.Mod.EventHandler
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import org.apache.logging.log4j.LogManager

@Suppress("unused")
@Mod(
    modid = KotatsuLib.MOD_ID,
    name = KotatsuLib.MOD_NAME,
    version = KotatsuLib.MOD_VERSION,
    guiFactory = "com.github.kotatsu_rtm.kotatsulib.mc1_12_2.core.KotatsuLibConfigImpl\$Gui"
)
class KotatsuLibImpl {
    @EventHandler
    fun onFMLPreInit(event: FMLPreInitializationEvent) {
        if (event.side.isClient) KotatsuLib.updateDebugOutputState(KotatsuLibConfigImpl)
    }

    @EventHandler
    fun onFMLInit(event: FMLInitializationEvent) {
        val bothSideEventListener =
            listOf(
                KotatsuLibConfigImpl
            )
        val clientOnlyEventListener =
            listOf(
                ShaderManagerImpl
            )

        bothSideEventListener.forEach { MinecraftForge.EVENT_BUS.register(it) }
        if (event.side.isClient) clientOnlyEventListener.forEach { MinecraftForge.EVENT_BUS.register(it) }

        LogManager.getLogger().info(
            """
            Initialize KotatsuLib
            categoryForDevelopers: ${KotatsuLibConfigImpl.categoryForDevelopers}
            enableOpenGLDebugOutput: ${KotatsuLibConfigImpl.enableOpenGLDebugOutput.get().getOrThrow()}
        """.trimIndent()
        )
    }
}
