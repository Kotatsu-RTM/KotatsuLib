package com.github.kotatsu_rtm.kotatsulib.api.shader

import com.github.kotatsu_rtm.kotatsulib.api.gl.VBO
import com.github.kotatsu_rtm.kotatsulib.api.kotlinutil.InvokeBlockOnChange
import com.github.kotatsu_rtm.kotatsulib.api.model.DrawGroup
import com.github.kotatsu_rtm.kotatsulib.api.model.IboInfo
import dev.siro256.forgelib.rtm_glsl.BufferAllocator
import dev.siro256.forgelib.rtm_glsl.enum.GLBufferUsage
import dev.siro256.forgelib.rtm_glsl.wrapper.IndexBufferObject
import dev.siro256.forgelib.rtm_glsl.wrapper.VertexArrayObject
import org.joml.Matrix4f
import org.joml.Vector2f
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL15
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL43
import java.util.*
import kotlin.jvm.optionals.getOrNull

@Suppress("unused")
object ColoredShader : Shader<ColoredShader.RenderData>(
    ColoredShader::class.java.classLoader
        .getResourceAsStream("shader/colored.vsh")!!
        .readBytes().decodeToString(),
    ColoredShader::class.java.classLoader
        .getResourceAsStream("shader/colored.fsh")!!
        .readBytes().decodeToString()
) {
    //uniform
    private const val MODEL_VIEW_PROJECTION_MATRIX_LOCATION = 0
    private const val COLOR_LOCATION = 10
    private const val LIGHT_SAMPLER_LOCATION = 11
    private const val LIGHT_POSITION_LOCATION = 12
    private const val SHOULD_NOT_LIGHTING_LOCATION = 13
    private const val INVERSE_MODEL_MATRIX_LOCATION = 14

    // in
    private const val VERTEX_POSITION_LOCATION = 0
    private const val NORMAL_LOCATION = 1

    @Suppress("DuplicatedCode")
    override val vao by lazy {
        object : VertexArrayObject() {
            init {
                bind()

                GL43.glVertexAttribBinding(VERTEX_POSITION_LOCATION, 0)
                GL43.glVertexAttribFormat(VERTEX_POSITION_LOCATION, 3, GL11.GL_FLOAT, false, 0)
                GL20.glEnableVertexAttribArray(VERTEX_POSITION_LOCATION)

                GL43.glVertexAttribBinding(NORMAL_LOCATION, 0)
                GL43.glVertexAttribFormat(NORMAL_LOCATION, 3, GL11.GL_FLOAT, false, 3 * Float.SIZE_BYTES)
                GL20.glEnableVertexAttribArray(NORMAL_LOCATION)

                unbind()
            }
        }
    }

    private val matrixBuffer = BufferAllocator.createDirectFloatBuffer(16)

    override fun preDraw() {
        super.preDraw()

        GL20.glUniform1i(LIGHT_SAMPLER_LOCATION, 1)
    }

    @Suppress("DuplicatedCode")
    override fun draw() {
        var modelViewProjectionMatrix: Matrix4f by InvokeBlockOnChange {
            matrixBuffer.apply {
                rewind()
                it.get(this)
                rewind()
            }.let { buffer -> GL20.glUniformMatrix4(MODEL_VIEW_PROJECTION_MATRIX_LOCATION, false, buffer) }
        }
        var inverseModelMatrix: Matrix4f by InvokeBlockOnChange {
            matrixBuffer.apply {
                rewind()
                it.get(this)
                rewind()
            }.let { buffer -> GL20.glUniformMatrix4(INVERSE_MODEL_MATRIX_LOCATION, false, buffer) }
        }
        var vbo: VBO.VertexNormalUV by InvokeBlockOnChange { it.bind(0) }
        var lightMapUV: Vector2f by InvokeBlockOnChange { GL20.glUniform2f(LIGHT_POSITION_LOCATION, it.x, it.y) }
        var color: UInt by InvokeBlockOnChange {
            GL20.glUniform4f(
                COLOR_LOCATION,
                (it shr 24).toFloat() / 255.0F,
                (it shr 16 and 0xffu).toFloat() / 255.0F,
                (it shr 8 and 0xffu).toFloat() / 255.0F,
                (it and 0xffu).toFloat() / 255.0F
            )
        }
        var ibo: IndexBufferObject by InvokeBlockOnChange { it.bind() }
        var shouldNotLighting: Boolean by InvokeBlockOnChange {
            GL20.glUniform1f(SHOULD_NOT_LIGHTING_LOCATION, if (it) 1.0f else 0.0f)
        }

        callBuffer.forEach {
            modelViewProjectionMatrix = it.modelViewProjectionMatrix
            inverseModelMatrix = it.inverseModelMatrix
            vbo = it.vbo
            lightMapUV = it.lightMapUV
            color = it.color
            shouldNotLighting = it.disableLighting

            if (it is RenderData.Buffered) {
                ibo = it.ibo

                GL11.glDrawElements(
                    GL11.GL_TRIANGLES,
                    it.iboInfoToDraw.size,
                    GL11.GL_UNSIGNED_INT,
                    it.iboInfoToDraw.offset.toLong() * Int.SIZE_BYTES
                )
            } else if (it is RenderData.NonBuffered) {
                val temporaryIBO =
                    object : IndexBufferObject(GLBufferUsage.GL_DYNAMIC_DRAW, it.indices) {
                        fun delete() {
                            GL15.glDeleteBuffers(name)
                        }
                    }
                ibo = temporaryIBO

                GL11.glDrawElements(
                    GL11.GL_TRIANGLES,
                    it.indices.size,
                    GL11.GL_UNSIGNED_INT,
                    0
                )

                temporaryIBO.delete()
            }
        }

        callBuffer.clear()
    }

    fun setViewAndProjectionMatrix(viewMatrix: Matrix4f, projectionMatrix: Matrix4f) =
        Builder<Matrix4f, Nothing, Nothing, Nothing, Nothing, Nothing, Nothing>(
            Optional.of(Matrix4f(viewMatrix)), Optional.of(Matrix4f(projectionMatrix))
        )

    interface RenderData : dev.siro256.forgelib.rtm_glsl.shader.Shader.RenderData {
        val modelViewProjectionMatrix: Matrix4f
        val inverseModelMatrix: Matrix4f
        val vbo: VBO.VertexNormalUV
        val lightMapUV: Vector2f
        val color: UInt
        val disableLighting: Boolean

        data class Buffered(
            override val modelViewProjectionMatrix: Matrix4f,
            override val inverseModelMatrix: Matrix4f,
            override val vbo: VBO.VertexNormalUV,
            override val lightMapUV: Vector2f,
            override val color: UInt,
            override val disableLighting: Boolean,
            val ibo: IndexBufferObject,
            val iboInfoToDraw: IboInfo,
        ) : RenderData

        data class NonBuffered(
            override val modelViewProjectionMatrix: Matrix4f,
            override val inverseModelMatrix: Matrix4f,
            override val vbo: VBO.VertexNormalUV,
            override val lightMapUV: Vector2f,
            override val color: UInt,
            override val disableLighting: Boolean,
            val indices: IntArray,
        ) : RenderData {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as NonBuffered

                if (modelViewProjectionMatrix != other.modelViewProjectionMatrix) return false
                if (vbo != other.vbo) return false
                if (lightMapUV != other.lightMapUV) return false
                if (color != other.color) return false
                return indices.contentEquals(other.indices)
            }

            override fun hashCode(): Int {
                var result = modelViewProjectionMatrix.hashCode()
                result = 31 * result + vbo.hashCode()
                result = 31 * result + lightMapUV.hashCode()
                result = 31 * result + color.hashCode()
                result = 31 * result + indices.contentHashCode()
                return result
            }
        }
    }

    data class Builder<A : Any, B : Any, C : Any, D : Any, E : Any, F : Any, G : Any>(
        private val viewMatrix: Optional<A> = Optional.empty(),
        private val projectionMatrix: Optional<A> = Optional.empty(),
        private val material: Optional<B> = Optional.empty(),
        private val vbo: Optional<C> = Optional.empty(),
        private val lightMapUV: Optional<D> = Optional.empty(),
        private val modelMatrix: Optional<E> = Optional.empty(),
        private val color: Optional<F> = Optional.empty(),
        private val drawGroupOrIndices: Optional<G> = Optional.empty(),
    ) {
        companion object {
            fun Builder<Matrix4f, Nothing, Nothing, Nothing, Nothing, Nothing, Nothing>.setMaterial(id: Int) =
                Builder<Matrix4f, Int, Nothing, Nothing, Nothing, Nothing, Nothing>(
                    viewMatrix, projectionMatrix,
                    Optional.of(id)
                )

            fun Builder<Matrix4f, Int, Nothing, Nothing, Nothing, Nothing, Nothing>.bindVBO(vbo: VBO.VertexNormalUV) =
                Builder<Matrix4f, Int, VBO.VertexNormalUV, Nothing, Nothing, Nothing, Nothing>(
                    viewMatrix, projectionMatrix, material,
                    Optional.of(vbo)
                )

            fun Builder<Matrix4f, Int, VBO.VertexNormalUV, Nothing, Nothing, Nothing, Nothing>.setLightMapCoords(
                uv: Vector2f,
            ) =
                Builder<Matrix4f, Int, VBO.VertexNormalUV, Vector2f, Nothing, Nothing, Nothing>(
                    viewMatrix, projectionMatrix, material, vbo,
                    Optional.of(uv)
                )

            fun Builder<Matrix4f, Int, VBO.VertexNormalUV, Vector2f, Nothing, Nothing, Nothing>.setModelMatrix(
                modelMatrix: Matrix4f,
            ) =
                Builder<Matrix4f, Int, VBO.VertexNormalUV, Vector2f, Matrix4f, Nothing, Nothing>(
                    viewMatrix, projectionMatrix, material, vbo, lightMapUV,
                    Optional.of(Matrix4f(modelMatrix))
                )

            fun Builder<Matrix4f, Int, VBO.VertexNormalUV, Vector2f, Matrix4f, Nothing, Nothing>.setColor(color: UInt) =
                Builder<Matrix4f, Int, VBO.VertexNormalUV, Vector2f, Matrix4f, UInt, Nothing>(
                    viewMatrix, projectionMatrix, material, vbo, lightMapUV, modelMatrix,
                    Optional.of(color)
                )

            fun Builder<Matrix4f, Int, VBO.VertexNormalUV, Vector2f, Matrix4f, UInt, Nothing>.useModel(
                model: DrawGroup,
            ) =
                Builder(
                    viewMatrix, projectionMatrix, material, vbo, lightMapUV, modelMatrix, color,
                    Optional.of(model)
                )

            fun Builder<Matrix4f, Int, VBO.VertexNormalUV, Vector2f, Matrix4f, UInt, Nothing>.setIndices(
                indices: IntArray,
            ) =
                Builder(
                    viewMatrix, projectionMatrix, material, vbo, lightMapUV, modelMatrix, color,
                    Optional.of(indices)
                )

            @JvmName("renderModel")
            fun Builder<Matrix4f, Int, VBO.VertexNormalUV, Vector2f, Matrix4f, UInt, DrawGroup>.render(
                disableLighting: Boolean = false,
            ) =
                also {
                    val drawGroup = drawGroupOrIndices.get()
                    val indicesInfo = drawGroup.getIndices(material.get()).getOrNull() ?: return@also
                    val modelMatrix = Matrix4f(modelMatrix.get())
                    val modelViewProjectionMatrix =
                        Matrix4f(projectionMatrix.get()).mul(viewMatrix.get()).mul(modelMatrix)

                    callBuffer.add(
                        RenderData.Buffered(
                            modelViewProjectionMatrix,
                            modelMatrix.invert(),
                            vbo.get(),
                            lightMapUV.get(),
                            color.get(),
                            disableLighting,
                            drawGroup.ibo,
                            indicesInfo
                        )
                    )
                }
                    .let {
                        Builder(
                            viewMatrix, projectionMatrix, material, vbo, lightMapUV, modelMatrix, color,
                            Optional.empty()
                        )
                    }

            @JvmName("renderIndices")
            fun Builder<Matrix4f, Int, VBO.VertexNormalUV, Vector2f, Matrix4f, UInt, IntArray>.render(
                disableLighting: Boolean = false,
            ) =
                also {
                    val modelMatrix = Matrix4f(modelMatrix.get())
                    val modelViewProjectionMatrix =
                        Matrix4f(projectionMatrix.get()).mul(Matrix4f(viewMatrix.get())).mul(Matrix4f(modelMatrix))

                    callBuffer.add(
                        RenderData.NonBuffered(
                            modelViewProjectionMatrix,
                            modelMatrix.invert(),
                            vbo.get(),
                            lightMapUV.get(),
                            color.get(),
                            disableLighting,
                            drawGroupOrIndices.get()
                        )
                    )
                }
                    .let {
                        Builder(
                            viewMatrix, projectionMatrix, material, vbo, lightMapUV, modelMatrix, color,
                            Optional.empty()
                        )
                    }

            @JvmName("bindVBO2")
            fun Builder<Matrix4f, Int, VBO.VertexNormalUV, Vector2f, Matrix4f, UInt, Any>.bindVBO(
                vbo: VBO.VertexNormalUV,
            ) =
                Builder<Matrix4f, Int, VBO.VertexNormalUV, Nothing, Nothing, Nothing, Nothing>(
                    viewMatrix, projectionMatrix, material,
                    Optional.of(vbo)
                )

            @JvmName("setLightMapCoords2")
            fun Builder<Matrix4f, Int, VBO.VertexNormalUV, Vector2f, Matrix4f, UInt, Any>.setLightMapCoords(
                uv: Vector2f,
            ) =
                Builder<Matrix4f, Int, VBO.VertexNormalUV, Vector2f, Nothing, Nothing, Nothing>(
                    viewMatrix, projectionMatrix, material, vbo,
                    Optional.of(uv)
                )

            @JvmName("setModelMatrix2")
            fun Builder<Matrix4f, Int, VBO.VertexNormalUV, Vector2f, Matrix4f, UInt, Any>.setModelMatrix(
                modelMatrix: Matrix4f,
            ) =
                Builder<Matrix4f, Int, VBO.VertexNormalUV, Vector2f, Matrix4f, Nothing, Nothing>(
                    viewMatrix, projectionMatrix, material, vbo, lightMapUV,
                    Optional.of(Matrix4f(modelMatrix))
                )

            @JvmName("setColor2")
            fun Builder<Matrix4f, Int, VBO.VertexNormalUV, Vector2f, Matrix4f, UInt, Any>.setColor(color: UInt) =
                Builder<Matrix4f, Int, VBO.VertexNormalUV, Vector2f, Matrix4f, UInt, Nothing>(
                    viewMatrix, projectionMatrix, material, vbo, lightMapUV, modelMatrix,
                    Optional.of(color)
                )

            @JvmName("useModel2")
            fun Builder<Matrix4f, Int, VBO.VertexNormalUV, Vector2f, Matrix4f, UInt, Any>.useModel(model: DrawGroup) =
                Builder(
                    viewMatrix, projectionMatrix, material, vbo, lightMapUV, modelMatrix, color,
                    Optional.of(model)
                )

            @JvmName("setIndices2")
            fun Builder<Matrix4f, Int, VBO.VertexNormalUV, Vector2f, Matrix4f, UInt, Any>.setIndices(
                indices: IntArray,
            ) =
                Builder(
                    viewMatrix, projectionMatrix, material, vbo, lightMapUV, modelMatrix, color,
                    Optional.of(indices)
                )
        }
    }
}
