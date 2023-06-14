package com.github.kotatsu_rtm.kotatsulib.api.model

import com.github.kotatsu_rtm.kotatsulib.api.gl.VBO
import com.github.kotatsu_rtm.kotatsulib.api.model.native.NativeFaceWrapper
import dev.siro256.forgelib.rtm_glsl.enum.GLBufferUsage
import dev.siro256.forgelib.rtm_glsl.format.VertexFormat
import java.util.*

abstract class ModelApi(nativeObjects: Map<String, List<NativeFaceWrapper>>) {
    private val objects: List<ModelObject>

    @Suppress("MemberVisibilityCanBePrivate")
    val vbo: VBO

    @Suppress("unused")
    fun generateDrawGroup(vararg objectNames: String): DrawGroup {
        val combinedMaterialIndicesPair =
            objectNames
                .mapNotNull { name -> objects.firstOrNull { it.name == name } }
                .flatMap { it.materialIndicesPair.entries }
                .let { entries ->
                    val mergedMap = mutableMapOf<Int, MutableList<Triple<Int, Int, Int>>>()
                    entries.forEach {
                        mergedMap
                            .getOrPut(it.key) { mutableListOf() }
                            .addAll(it.value)
                    }
                    mergedMap
                }

        return DrawGroup(combinedMaterialIndicesPair)
    }

    @Suppress("unused")
    fun getObject(name: String) = Optional.ofNullable(objects.firstOrNull { it.name == name })

    init {
        val modelObjects = mutableListOf<ModelObject>()
        val wrappedObjects = mutableListOf<VertexFormat.VertexNormalUV>()

        nativeObjects.forEach { (name, faces) ->
            val materialIndicesPair = mutableMapOf<Int, MutableList<Triple<Int, Int, Int>>>()

            faces.forEach {
                val verticesOffset = wrappedObjects.size
                materialIndicesPair
                    .getOrPut(it.materialId) { mutableListOf() }
                    .add(Triple(verticesOffset, verticesOffset + 1, verticesOffset + 2))

                wrappedObjects.addAll(it.vertices.toList())
            }

            modelObjects.add(ModelObject(name, materialIndicesPair))
        }

        objects = modelObjects
        vbo = VBO.VertexNormalUV(GLBufferUsage.GL_STATIC_DRAW, wrappedObjects)
    }
}
