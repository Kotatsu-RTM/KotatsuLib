package com.github.kotatsu_rtm.kotatsulib.api.gl

import dev.siro256.forgelib.rtm_glsl.enum.GLBufferUsage
import dev.siro256.forgelib.rtm_glsl.format.VertexFormat
import dev.siro256.forgelib.rtm_glsl.wrapper.VertexBufferObject

interface VBO {
    class Vertex(objects: List<VertexFormat.Vertex>, usage: GLBufferUsage) : VertexBufferObject(
        usage,
        objects.flatMap { it.toList() }.toFloatArray(),
        VertexFormat.Vertex.SIZE_BYTES
    )

    class VertexUV(objects: List<VertexFormat.VertexUV>, usage: GLBufferUsage) : VertexBufferObject(
        usage,
        objects.flatMap { it.toList() }.toFloatArray(),
        VertexFormat.VertexUV.SIZE_BYTES
    )

    class VertexNormalUV(objects: List<VertexFormat.VertexNormalUV>, usage: GLBufferUsage) : VertexBufferObject(
        usage,
        objects.flatMap { it.toList() }.toFloatArray(),
        VertexFormat.VertexNormalUV.SIZE_BYTES
    )
}
