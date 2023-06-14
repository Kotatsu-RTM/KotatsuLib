package com.github.kotatsu_rtm.kotatsulib.api.gl

import dev.siro256.forgelib.rtm_glsl.enum.GLBufferUsage
import dev.siro256.forgelib.rtm_glsl.format.VertexFormat
import dev.siro256.forgelib.rtm_glsl.wrapper.VertexBufferObject

interface VBO {
    class Vertex(usage: GLBufferUsage, objects: List<VertexFormat.Vertex>) : VBO, VertexBufferObject(
        usage,
        objects.flatMap { it.toList() }.toFloatArray(),
        VertexFormat.Vertex.SIZE_BYTES
    )

    class VertexUV(usage: GLBufferUsage, objects: List<VertexFormat.VertexUV>) : VBO, VertexBufferObject(
        usage,
        objects.flatMap { it.toList() }.toFloatArray(),
        VertexFormat.VertexUV.SIZE_BYTES
    )

    class VertexNormalUV(usage: GLBufferUsage, objects: List<VertexFormat.VertexNormalUV>) : VBO, VertexBufferObject(
        usage,
        objects.flatMap { it.toList() }.toFloatArray(),
        VertexFormat.VertexNormalUV.SIZE_BYTES
    )
}
