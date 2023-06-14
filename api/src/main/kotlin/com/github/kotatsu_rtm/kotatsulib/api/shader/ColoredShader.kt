package com.github.kotatsu_rtm.kotatsulib.api.shader

import com.github.kotatsu_rtm.kotatsulib.api.gl.VBO
import com.github.kotatsu_rtm.kotatsulib.api.kotlinutil.InvokeBlockOnChange
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

@Suppress("unused")
object ColoredShader : Shader<ColoredShader.RenderData>(
    ColoredShader::class.java.classLoader
        .getResourceAsStream("shaders/colored.vsh")!!
        .readBytes().decodeToString(),
    ColoredShader::class.java.classLoader
        .getResourceAsStream("shaders/colored.fsh")!!
        .readBytes().decodeToString()
) {
    //uniform
    private const val MODEL_VIEW_PROJECTION_MATRIX_LOCATION = 0
    private const val COLOR_LOCATION = 10
    private const val LIGHT_SAMPLER_LOCATION = 11
    private const val LIGHT_POSITION_LOCATION = 12

    // in
    private const val VERTEX_POSITION_LOCATION = 0
    private const val NORMAL_LOCATION = 1

    override val vao =
        object : VertexArrayObject() {
            init {
                bind()

                GL43.glVertexAttribBinding(VERTEX_POSITION_LOCATION, 0)
                GL43.glVertexAttribFormat(VERTEX_POSITION_LOCATION, 3, GL11.GL_FLOAT, false, 0)
                GL20.glEnableVertexAttribArray(VERTEX_POSITION_LOCATION)

                GL43.glVertexAttribBinding(NORMAL_LOCATION, 0)
                GL43.glVertexAttribFormat(NORMAL_LOCATION, 3, GL11.GL_FLOAT, false, 6 * Float.SIZE_BYTES)
                GL20.glEnableVertexAttribArray(NORMAL_LOCATION)

                unbind()
            }
        }

    private val matrixBuffer = BufferAllocator.createDirectFloatBuffer(16)

    override fun preDraw() {
        super.preDraw()

        GL20.glUniform1i(LIGHT_SAMPLER_LOCATION, 1)
    }

    override fun draw() {
        var modelViewProjectionMatrix: Matrix4f by InvokeBlockOnChange {
            matrixBuffer.apply {
                rewind()
                it.get(this)
                rewind()
            }.let { buffer -> GL20.glUniformMatrix4(MODEL_VIEW_PROJECTION_MATRIX_LOCATION, false, buffer) }
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

        callBuffer.forEach {
            modelViewProjectionMatrix = it.modelViewProjectionMatrix
            vbo = it.vbo
            lightMapUV = it.lightMapUV
            color = it.color

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

    fun updateProjection(matrix: Matrix4f) =
        Builder<Matrix4f, Nothing, Nothing, Nothing, Nothing, Nothing>(projectionMatrix = Optional.of(matrix))

    fun Builder<Matrix4f, Nothing, Nothing, Nothing, Nothing, Nothing>.bindVBO(vbo: VBO.VertexNormalUV) =
        Builder<Matrix4f, VBO.VertexNormalUV, Nothing, Nothing, Nothing, Nothing>(
            projectionMatrix,
            Optional.of(vbo)
        )

    fun Builder<Matrix4f, VBO.VertexNormalUV, Nothing, Nothing, Nothing, Nothing>.setLightMapCoords(uv: Vector2f) =
        Builder<Matrix4f, VBO.VertexNormalUV, Vector2f, Nothing, Nothing, Nothing>(
            projectionMatrix, vbo,
            Optional.of(uv)
        )

    fun Builder<Matrix4f, VBO.VertexNormalUV, Vector2f, Nothing, Nothing, Nothing>.setModelView(matrix: Matrix4f) =
        Builder<Matrix4f, VBO.VertexNormalUV, Vector2f, Matrix4f, Nothing, Nothing>(
            projectionMatrix, vbo, lightMapUV,
            Optional.of(matrix)
        )

    fun Builder<Matrix4f, VBO.VertexNormalUV, Vector2f, Matrix4f, Nothing, Nothing>.setColor(color: UInt) =
        Builder<Matrix4f, VBO.VertexNormalUV, Vector2f, Matrix4f, UInt, Nothing>(
            projectionMatrix, vbo, lightMapUV, modelViewMatrix,
            Optional.of(color)
        )

    fun Builder<Matrix4f, VBO.VertexNormalUV, Vector2f, Matrix4f, UInt, Nothing>.useModel(
        ibo: IndexBufferObject,
        drawInfo: IboInfo,
    ) = Builder(projectionMatrix, vbo, lightMapUV, modelViewMatrix, color, Optional.of(ibo to drawInfo))

    fun Builder<Matrix4f, VBO.VertexNormalUV, Vector2f, Matrix4f, UInt, Nothing>.setIndices(indices: IntArray) =
        Builder(projectionMatrix, vbo, lightMapUV, modelViewMatrix, color, Optional.of(indices))

    fun Builder<Matrix4f, VBO.VertexNormalUV, Vector2f, Matrix4f, UInt, Pair<IndexBufferObject, IboInfo>>.render() =
        also {
            val clonedProjectionMatrix = Matrix4f(projectionMatrix.get())
            val modelViewProjectionMatrix = clonedProjectionMatrix.mul(modelViewMatrix.get())
            val iboOrIndices = iboOrIndices.get()

            callBuffer.add(
                RenderData.Buffered(
                    modelViewProjectionMatrix,
                    vbo.get(),
                    lightMapUV.get(),
                    color.get(),
                    iboOrIndices.first,
                    iboOrIndices.second
                )
            )
        }
            .let { Builder(projectionMatrix, vbo, lightMapUV, modelViewMatrix, color, Optional.empty()) }

    fun Builder<Matrix4f, VBO.VertexNormalUV, Vector2f, Matrix4f, UInt, IntArray>.render() =
        also {
            val clonedProjectionMatrix = Matrix4f(projectionMatrix.get())
            val modelViewProjectionMatrix = clonedProjectionMatrix.mul(modelViewMatrix.get())

            callBuffer.add(
                RenderData.NonBuffered(
                    modelViewProjectionMatrix,
                    vbo.get(),
                    lightMapUV.get(),
                    color.get(),
                    iboOrIndices.get()
                )
            )
        }
            .let { Builder(projectionMatrix, vbo, lightMapUV, modelViewMatrix, color, Optional.empty()) }

    fun Builder<Matrix4f, VBO.VertexNormalUV, Vector2f, Matrix4f, UInt, Any>.bindVBO(vbo: VBO.VertexNormalUV) =
        Builder<Matrix4f, VBO.VertexNormalUV, Nothing, Nothing, Nothing, Nothing>(
            projectionMatrix,
            Optional.of(vbo)
        )

    fun Builder<Matrix4f, VBO.VertexNormalUV, Vector2f, Matrix4f, UInt, Any>.setLightMapCoords(uv: Vector2f) =
        Builder<Matrix4f, VBO.VertexNormalUV, Vector2f, Nothing, Nothing, Nothing>(
            projectionMatrix, vbo,
            Optional.of(uv)
        )

    fun Builder<Matrix4f, VBO.VertexNormalUV, Vector2f, Matrix4f, UInt, Any>.setModelView(matrix: Matrix4f) =
        Builder<Matrix4f, VBO.VertexNormalUV, Vector2f, Matrix4f, Nothing, Nothing>(
            projectionMatrix, vbo, lightMapUV,
            Optional.of(matrix)
        )

    fun Builder<Matrix4f, VBO.VertexNormalUV, Vector2f, Matrix4f, UInt, Any>.setColor(color: UInt) =
        Builder<Matrix4f, VBO.VertexNormalUV, Vector2f, Matrix4f, UInt, Nothing>(
            projectionMatrix, vbo, lightMapUV, modelViewMatrix,
            Optional.of(color)
        )

    fun Builder<Matrix4f, VBO.VertexNormalUV, Vector2f, Matrix4f, UInt, Any>.useModel(
        ibo: IndexBufferObject,
        drawInfo: IboInfo,
    ) = Builder(projectionMatrix, vbo, lightMapUV, modelViewMatrix, color, Optional.of(ibo to drawInfo))

    fun Builder<Matrix4f, VBO.VertexNormalUV, Vector2f, Matrix4f, UInt, Any>.setIndices(indices: IntArray) =
        Builder(projectionMatrix, vbo, lightMapUV, modelViewMatrix, color, Optional.of(indices))

    interface RenderData : dev.siro256.forgelib.rtm_glsl.shader.Shader.RenderData {
        val modelViewProjectionMatrix: Matrix4f
        val vbo: VBO.VertexNormalUV
        val lightMapUV: Vector2f
        val color: UInt

        data class Buffered(
            override val modelViewProjectionMatrix: Matrix4f,
            override val vbo: VBO.VertexNormalUV,
            override val lightMapUV: Vector2f,
            override val color: UInt,
            val ibo: IndexBufferObject,
            val iboInfoToDraw: IboInfo,
        ) : RenderData

        data class NonBuffered(
            override val modelViewProjectionMatrix: Matrix4f,
            override val vbo: VBO.VertexNormalUV,
            override val lightMapUV: Vector2f,
            override val color: UInt,
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
                if (!indices.contentEquals(other.indices)) return false

                return true
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

    data class Builder<A : Any, B : Any, C : Any, D : Any, E : Any, F : Any>(
        val projectionMatrix: Optional<A> = Optional.empty(),
        val vbo: Optional<B> = Optional.empty(),
        val lightMapUV: Optional<C> = Optional.empty(),
        val modelViewMatrix: Optional<D> = Optional.empty(),
        val color: Optional<E> = Optional.empty(),
        val iboOrIndices: Optional<F> = Optional.empty(),
    )
}
