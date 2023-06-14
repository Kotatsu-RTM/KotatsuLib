package com.github.kotatsu_rtm.kotatsulib.api.model.native

import dev.siro256.forgelib.rtm_glsl.format.VertexFormat

data class NativeFaceWrapper(
    val materialId: Int,
    val vertices: Triple<VertexFormat.VertexNormalUV, VertexFormat.VertexNormalUV, VertexFormat.VertexNormalUV>
)
