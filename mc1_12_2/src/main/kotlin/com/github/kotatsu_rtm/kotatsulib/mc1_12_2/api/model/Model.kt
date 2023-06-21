package com.github.kotatsu_rtm.kotatsulib.mc1_12_2.api.model

import com.github.kotatsu_rtm.kotatsulib.api.model.ModelApi
import com.github.kotatsu_rtm.kotatsulib.api.model.native.NativeFaceWrapper
import dev.siro256.forgelib.rtm_glsl.format.VertexFormat
import jp.ngt.ngtlib.renderer.model.GroupObject

@Suppress("unused")
abstract class Model(groupObjects: List<GroupObject>) : ModelApi(transformGroupObjects(groupObjects)) {
    companion object {
        fun transformGroupObjects(groupObjects: List<GroupObject>) =
            groupObjects.associate { groupObject ->
                groupObject.name to groupObject.faces.map {
                    fun getVertexNormalUV(index: Int) =
                        VertexFormat.VertexNormalUV(
                            it.vertices[index].x, it.vertices[index].y, it.vertices[index].z,
                            it.vertexNormals[index].x, it.vertexNormals[index].y, it.vertexNormals[index].z,
                            it.textureCoordinates[index].u, it.textureCoordinates[index].v
                        )

                    NativeFaceWrapper(
                        it.materialId.toInt(),
                        Triple(getVertexNormalUV(0), getVertexNormalUV(1), getVertexNormalUV(2))
                    )
                }
            }
    }
}
