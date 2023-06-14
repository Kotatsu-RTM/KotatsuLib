package com.github.kotatsu_rtm.kotatsulib.api.model

import dev.siro256.forgelib.rtm_glsl.enum.GLBufferUsage
import dev.siro256.forgelib.rtm_glsl.wrapper.IndexBufferObject
import java.util.*

class DrawGroup(materialIndicesPair: MaterialIndicesPair) {
    private val iboInfos =
        materialIndicesPair
            .filterValues { it.isNotEmpty() }
            .let { pair ->
                var offset = 0
                pair.map {
                    val size = it.value.size * 3
                    val iboInfo = IboInfo(offset, size)
                    offset += size

                    it.key to iboInfo
                }
            }

    @Suppress("unused")
    val ibo =
        IndexBufferObject(
            GLBufferUsage.GL_STATIC_DRAW,
            materialIndicesPair.flatMap { it.value }.flatMap { it.toList() }.toIntArray()
        )

    @Suppress("unused")
    fun getIndices(materialId: Int) = Optional.ofNullable(iboInfos[materialId])
}
