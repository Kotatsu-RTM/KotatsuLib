package com.github.kotatsu_rtm.kotatsulib.api.shader

import com.github.kotatsu_rtm.kotatsulib.api.gl.VBO
import com.github.kotatsu_rtm.kotatsulib.api.kotlinutil.InvokeBlockOnChange
import com.github.kotatsu_rtm.kotatsulib.api.model.DrawGroup
import com.github.kotatsu_rtm.kotatsulib.api.model.IboInfo
import dev.siro256.forgelib.rtm_glsl.BufferAllocator
import dev.siro256.forgelib.rtm_glsl.wrapper.IndexBufferObject
import dev.siro256.forgelib.rtm_glsl.wrapper.VertexArrayObject
import org.joml.Matrix4f
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL43
import java.util.*
import kotlin.jvm.optionals.getOrNull

object TexturedWithColorShader : Shader<TexturedWithColorShader.RenderData>(
    TexturedWithColorShader::class.java.classLoader
        .getResourceAsStream("shader/textured_with_color.vsh")!!
        .readBytes().decodeToString(),
    TexturedWithColorShader::class.java.classLoader
        .getResourceAsStream("shader/textured_with_color.fsh")!!
        .readBytes().decodeToString()
) {
    //uniform
    private const val MODEL_VIEW_PROJECTION_MATRIX_LOCATION = 0
    private const val TEXTURE_SAMPLER_LOCATION = 10
    private const val COLOR_LOCATION = 11

    //in
    private const val VERTEX_POSITION_LOCATION = 0
    private const val TEXTURE_POSITION_LOCATION = 1

    private val matrixBuffer = BufferAllocator.createDirectFloatBuffer(16)

    override val vao by lazy {
        object : VertexArrayObject() {
            init {
                bind()

                GL43.glVertexAttribBinding(VERTEX_POSITION_LOCATION, 0)
                GL43.glVertexAttribFormat(VERTEX_POSITION_LOCATION, 3, GL11.GL_FLOAT, false, 0)
                GL20.glEnableVertexAttribArray(VERTEX_POSITION_LOCATION)

                GL43.glVertexAttribBinding(TEXTURE_POSITION_LOCATION, 0)
                GL43.glVertexAttribFormat(TEXTURE_POSITION_LOCATION, 2, GL11.GL_FLOAT, false, 6 * Float.SIZE_BYTES)
                GL20.glEnableVertexAttribArray(TEXTURE_POSITION_LOCATION)

                unbind()
            }
        }
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
        var vbo: VBO.VertexNormalUV by InvokeBlockOnChange { it.bind(0) }
        var ibo: IndexBufferObject by InvokeBlockOnChange { it.bind() }
        var texture: Int by InvokeBlockOnChange { GL11.glBindTexture(GL11.GL_TEXTURE_2D, it) }
        var color: UInt by InvokeBlockOnChange {
            GL20.glUniform4f(
                COLOR_LOCATION,
                (it shr 24).toFloat() / 255.0F,
                (it shr 16 and 0xffu).toFloat() / 255.0F,
                (it shr 8 and 0xffu).toFloat() / 255.0F,
                (it and 0xffu).toFloat() / 255.0F
            )
        }

        fun processDraw(renderData: RenderData) {
            modelViewProjectionMatrix = renderData.modelViewProjectionMatrix
            vbo = renderData.vbo
            ibo = renderData.ibo
            texture = renderData.textureName
            color = renderData.color

            GL11.glDrawElements(
                GL11.GL_TRIANGLES,
                renderData.iboInfoToDraw.size,
                GL11.GL_UNSIGNED_INT,
                renderData.iboInfoToDraw.offset.toLong() * Int.SIZE_BYTES
            )
        }

        callBuffer.filterNot(RenderData::hasAlpha).forEach(::processDraw)

        val cullingState = GL11.glGetBoolean(GL11.GL_CULL_FACE)
        val alphaTestState = GL11.glGetBoolean(GL11.GL_ALPHA_TEST)
        val blendState = GL11.glGetBoolean(GL11.GL_BLEND)
        val srcBlendFunction = GL11.glGetInteger(GL11.GL_BLEND_SRC)
        val dstBlendFunction = GL11.glGetInteger(GL11.GL_BLEND_DST)

        GL11.glDepthMask(false)
        if (cullingState) GL11.glDisable(GL11.GL_CULL_FACE)
        if (alphaTestState) GL11.glDisable(GL11.GL_ALPHA_TEST)
        if (!blendState) GL11.glEnable(GL11.GL_BLEND)
        if (srcBlendFunction != GL11.GL_SRC_ALPHA || dstBlendFunction != GL11.GL_ONE_MINUS_SRC_ALPHA)
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

        callBuffer.filter(RenderData::hasAlpha).forEach(::processDraw)

        GL11.glDepthMask(true)
        if (cullingState) GL11.glEnable(GL11.GL_CULL_FACE)
        if (alphaTestState) GL11.glEnable(GL11.GL_ALPHA_TEST)
        if (!blendState) GL11.glDisable(GL11.GL_BLEND)
        if (srcBlendFunction != GL11.GL_SRC_ALPHA || dstBlendFunction != GL11.GL_ONE_MINUS_SRC_ALPHA)
            GL11.glBlendFunc(srcBlendFunction, dstBlendFunction)

        callBuffer.clear()
    }

    fun updateProjection(matrix: Matrix4f) =
        Builder<Matrix4f, Nothing, Nothing, Nothing, Nothing, Nothing, Nothing>(Optional.of(matrix))

    data class RenderData(
        val modelViewProjectionMatrix: Matrix4f,
        val vbo: VBO.VertexNormalUV,
        val textureName: Int,
        val color: UInt,
        val ibo: IndexBufferObject,
        val iboInfoToDraw: IboInfo,
        val hasAlpha: Boolean,
    ) : dev.siro256.forgelib.rtm_glsl.shader.Shader.RenderData

    data class Builder<A : Any, B : Any, C : Any, D : Any, E : Any, F : Any, G : Any>(
        private val projectionMatrix: Optional<A> = Optional.empty(),
        private val material: Optional<B> = Optional.empty(),
        private val textureName: Optional<C> = Optional.empty(),
        private val vbo: Optional<D> = Optional.empty(),
        private val color: Optional<E> = Optional.empty(),
        private val modelViewMatrix: Optional<F> = Optional.empty(),
        private val model: Optional<G> = Optional.empty(),
    ) {
        companion object {
            fun Builder<Matrix4f, Nothing, Nothing, Nothing, Nothing, Nothing, Nothing>.setMaterial(id: Int) =
                Builder<Matrix4f, Int, Nothing, Nothing, Nothing, Nothing, Nothing>(
                    projectionMatrix,
                    Optional.of(id)
                )

            fun Builder<Matrix4f, Int, Nothing, Nothing, Nothing, Nothing, Nothing>.setTexture(name: Int) =
                Builder<Matrix4f, Int, Int, Nothing, Nothing, Nothing, Nothing>(
                    projectionMatrix, material,
                    Optional.of(name)
                )

            fun Builder<Matrix4f, Int, Int, Nothing, Nothing, Nothing, Nothing>.bindVBO(vbo: VBO.VertexNormalUV) =
                Builder<Matrix4f, Int, Int, VBO.VertexNormalUV, Nothing, Nothing, Nothing>(
                    projectionMatrix, material, textureName,
                    Optional.of(vbo)
                )

            fun Builder<Matrix4f, Int, Int, VBO.VertexNormalUV, Nothing, Nothing, Nothing>.setColor(color: UInt) =
                Builder<Matrix4f, Int, Int, VBO.VertexNormalUV, UInt, Nothing, Nothing>(
                    projectionMatrix, material, textureName, vbo,
                    Optional.of(color)
                )

            fun Builder<Matrix4f, Int, Int, VBO.VertexNormalUV, UInt, Nothing, Nothing>.setModelView(matrix: Matrix4f) =
                Builder<Matrix4f, Int, Int, VBO.VertexNormalUV, UInt, Matrix4f, Nothing>(
                    projectionMatrix, material, textureName, vbo, color,
                    Optional.of(matrix)
                )

            fun Builder<Matrix4f, Int, Int, VBO.VertexNormalUV, UInt, Matrix4f, Nothing>.useModel(model: DrawGroup) =
                Builder(projectionMatrix, material, textureName, vbo, color, modelViewMatrix, Optional.of(model))

            @Suppress("DuplicatedCode")
            fun Builder<Matrix4f, Int, Int, VBO.VertexNormalUV, UInt, Matrix4f, DrawGroup>.render(
                hasAlpha: Boolean = false
            ) =
                also {
                    val model = model.get()
                    val indicesInfo = model.getIndices(material.get()).getOrNull() ?: return@also
                    val clonedProjectionMatrix = Matrix4f(projectionMatrix.get())
                    val modelViewProjectionMatrix = clonedProjectionMatrix.mul(modelViewMatrix.get())

                    callBuffer.add(
                        RenderData(
                            modelViewProjectionMatrix,
                            vbo.get(),
                            textureName.get(),
                            color.get(),
                            model.ibo,
                            indicesInfo,
                            hasAlpha
                        )
                    )
                }

            @JvmName("bindVBO2")
            fun Builder<Matrix4f, Int, Int, VBO.VertexNormalUV, UInt, Matrix4f, DrawGroup>.bindVBO(
                vbo: VBO.VertexNormalUV
            ) =
                Builder<Matrix4f, Int, Int, VBO.VertexNormalUV, Nothing, Nothing, Nothing>(
                    projectionMatrix, material, textureName,
                    Optional.of(vbo)
                )

            @JvmName("setColor3")
            fun Builder<Matrix4f, Int, Int, VBO.VertexNormalUV, UInt, Matrix4f, DrawGroup>.setColor(color: UInt) =
                Builder<Matrix4f, Int, Int, VBO.VertexNormalUV, UInt, Nothing, Nothing>(
                    projectionMatrix, material, textureName, vbo,
                    Optional.of(color)
                )

            @JvmName("setModelView2")
            fun Builder<Matrix4f, Int, Int, VBO.VertexNormalUV, UInt, Matrix4f, DrawGroup>.setModelView(
                matrix: Matrix4f
            ) =
                Builder<Matrix4f, Int, Int, VBO.VertexNormalUV, UInt, Matrix4f, Nothing>(
                    projectionMatrix, material, textureName, vbo, color,
                    Optional.of(matrix)
                )

            @JvmName("useModel2")
            fun Builder<Matrix4f, Int, Int, VBO.VertexNormalUV, UInt, Matrix4f, DrawGroup>.useModel(model: DrawGroup) =
                Builder(projectionMatrix, material, textureName, vbo, color, modelViewMatrix, Optional.of(model))
        }
    }
}
