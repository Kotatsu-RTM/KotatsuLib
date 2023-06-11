package com.github.kotatsu_rtm.kotatsulib.mc1_12_2.core

import com.github.kotatsu_rtm.kotatsulib.core.KotatsuLibConfig
import com.github.kotatsu_rtm.kotatsulib.mc1_12_2.api.config.type.ConfigTypeImpl
import net.minecraftforge.common.config.Configuration
import java.io.File

internal object KotatsuLibConfigImpl: KotatsuLibConfig<Configuration>(ConfigTypeImpl) {
    override val nativeInstance = Configuration(File(relativePathFromGameDirectory))
}
