package com.github.kotatsu_rtm.kotatsulib.api.shader

import com.github.kotatsu_rtm.kotatsulib.api.gl.VBO
import com.github.kotatsu_rtm.kotatsulib.api.kotlinutil.InvokeBlockOnChange
import com.github.kotatsu_rtm.kotatsulib.api.model.DrawGroup
import com.github.kotatsu_rtm.kotatsulib.api.model.IboInfo
import dev.siro256.forgelib.rtm_glsl.BufferAllocator
import dev.siro256.forgelib.rtm_glsl.wrapper.IndexBufferObject
import dev.siro256.forgelib.rtm_glsl.wrapper.VertexArrayObject
import org.joml.Matrix4f
import org.joml.Vector2f
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL43
import java.util.*
import kotlin.jvm.optionals.getOrNull

@Suppress("unused")
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
    private const val LIGHT_SAMPLER_LOCATION = 12
    private const val LIGHT_POSITION_LOCATION = 13
    private const val SHOULD_NOT_LIGHTING_LOCATION = 14
    private const val INVERSE_MODEL_MATRIX_LOCATION = 15

    //in
    private const val VERTEX_POSITION_LOCATION = 0
    private const val TEXTURE_POSITION_LOCATION = 1
    private const val NORMAL_LOCATION = 2

    private val matrixBuffer = BufferAllocator.createDirectFloatBuffer(16)

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

                GL43.glVertexAttribBinding(TEXTURE_POSITION_LOCATION, 0)
                GL43.glVertexAttribFormat(TEXTURE_POSITION_LOCATION, 2, GL11.GL_FLOAT, false, 6 * Float.SIZE_BYTES)
                GL20.glEnableVertexAttribArray(TEXTURE_POSITION_LOCATION)

                unbind()
            }
        }
    }

    override fun preDraw() {
        super.preDraw()

        GL20.glUniform1i(TEXTURE_SAMPLER_LOCATION, 0)
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
        var ibo: IndexBufferObject by InvokeBlockOnChange { it.bind() }
        var lightMapUV: Vector2f by InvokeBlockOnChange { GL20.glUniform2f(LIGHT_POSITION_LOCATION, it.x, it.y) }
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
        var shouldNotLighting: Boolean by InvokeBlockOnChange {
            GL20.glUniform1f(SHOULD_NOT_LIGHTING_LOCATION, if (it) 1.0f else 0.0f)
        }

        fun processDraw(renderData: RenderData) {
            modelViewProjectionMatrix = renderData.modelViewProjectionMatrix
            inverseModelMatrix = renderData.inverseModelMatrix
            vbo = renderData.vbo
            ibo = renderData.ibo
            lightMapUV = renderData.lightMapUV
            texture = renderData.textureName
            color = renderData.color
            shouldNotLighting = renderData.disableLighting

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

    fun setViewAndProjectionMatrix(viewMatrix: Matrix4f, projectionMatrix: Matrix4f) =
        Builder<Matrix4f, Nothing, Nothing, Nothing, Nothing, Nothing, Nothing, Nothing>(
            Optional.of(Matrix4f(viewMatrix)), Optional.of(Matrix4f(projectionMatrix))
        )

    data class RenderData(
        val modelViewProjectionMatrix: Matrix4f,
        val inverseModelMatrix: Matrix4f,
        val lightMapUV: Vector2f,
        val vbo: VBO.VertexNormalUV,
        val textureName: Int,
        val color: UInt,
        val ibo: IndexBufferObject,
        val iboInfoToDraw: IboInfo,
        val hasAlpha: Boolean,
        val disableLighting: Boolean,
    ) : dev.siro256.forgelib.rtm_glsl.shader.Shader.RenderData

    data class Builder<A : Any, B : Any, C : Any, D : Any, E : Any, F : Any, G : Any, H : Any>(
        private val viewMatrix: Optional<A> = Optional.empty(),
        private val projectionMatrix: Optional<A> = Optional.empty(),
        private val material: Optional<B> = Optional.empty(),
        private val textureName: Optional<C> = Optional.empty(),
        private val vbo: Optional<D> = Optional.empty(),
        private val lightMapUV: Optional<E> = Optional.empty(),
        private val modelMatrix: Optional<F> = Optional.empty(),
        private val color: Optional<G> = Optional.empty(),
        private val model: Optional<H> = Optional.empty(),
    ) {
        companion object {
            fun Builder<Matrix4f, Nothing, Nothing, Nothing, Nothing, Nothing, Nothing, Nothing>.setMaterial(id: Int) =
                Builder<Matrix4f, Int, Nothing, Nothing, Nothing, Nothing, Nothing, Nothing>(
                    viewMatrix, projectionMatrix,
                    Optional.of(id)
                )

            fun Builder<Matrix4f, Int, Nothing, Nothing, Nothing, Nothing, Nothing, Nothing>.setTexture(name: Int) =
                Builder<Matrix4f, Int, Int, Nothing, Nothing, Nothing, Nothing, Nothing>(
                    viewMatrix, projectionMatrix, material,
                    Optional.of(name)
                )

            fun Builder<Matrix4f, Int, Int, Nothing, Nothing, Nothing, Nothing, Nothing>.bindVBO(
                vbo: VBO.VertexNormalUV,
            ) =
                Builder<Matrix4f, Int, Int, VBO.VertexNormalUV, Nothing, Nothing, Nothing, Nothing>(
                    viewMatrix, projectionMatrix, material, textureName,
                    Optional.of(vbo)
                )

            fun Builder<Matrix4f, Int, Int, VBO.VertexNormalUV, Nothing, Nothing, Nothing, Nothing>.setLightMapCoords(
                uv: Vector2f,
            ) =
                Builder<Matrix4f, Int, Int, VBO.VertexNormalUV, Vector2f, Nothing, Nothing, Nothing>(
                    viewMatrix, projectionMatrix, material, textureName, vbo,
                    Optional.of(uv)
                )

            fun Builder<Matrix4f, Int, Int, VBO.VertexNormalUV, Vector2f, Nothing, Nothing, Nothing>.setModelMatrix(
                modelMatrix: Matrix4f,
            ) =
                Builder<Matrix4f, Int, Int, VBO.VertexNormalUV, Vector2f, Matrix4f, Nothing, Nothing>(
                    viewMatrix, projectionMatrix, material, textureName, vbo, lightMapUV,
                    Optional.of(Matrix4f(modelMatrix))
                )

            fun Builder<Matrix4f, Int, Int, VBO.VertexNormalUV, Vector2f, Matrix4f, Nothing, Nothing>.setColor(
                color: UInt,
            ) =
                Builder<Matrix4f, Int, Int, VBO.VertexNormalUV, Vector2f, Matrix4f, UInt, Nothing>(
                    viewMatrix, projectionMatrix, material, textureName, vbo, lightMapUV, modelMatrix,
                    Optional.of(color)
                )

            fun Builder<Matrix4f, Int, Int, VBO.VertexNormalUV, Vector2f, Matrix4f, UInt, Nothing>.useModel(
                model: DrawGroup,
            ) =
                Builder(
                    viewMatrix, projectionMatrix, material, textureName, vbo, lightMapUV, modelMatrix, color,
                    Optional.of(model)
                )

            @Suppress("DuplicatedCode")
            fun Builder<Matrix4f, Int, Int, VBO.VertexNormalUV, Vector2f, Matrix4f, UInt, DrawGroup>.render(
                hasAlpha: Boolean = false,
                disableLighting: Boolean = false,
            ) =
                also {
                    val model = model.get()
                    val indicesInfo = model.getIndices(material.get()).getOrNull() ?: return@also
                    val modelMatrix = Matrix4f(modelMatrix.get())
                    val modelViewProjectionMatrix =
                        Matrix4f(projectionMatrix.get()).mul(Matrix4f(viewMatrix.get())).mul(Matrix4f(modelMatrix))

                    callBuffer.add(
                        RenderData(
                            modelViewProjectionMatrix,
                            modelMatrix.invert(),
                            lightMapUV.get(),
                            vbo.get(),
                            textureName.get(),
                            color.get(),
                            model.ibo,
                            indicesInfo,
                            hasAlpha,
                            disableLighting
                        )
                    )
                }

            @JvmName("bindVBO2")
            fun Builder<Matrix4f, Int, Int, VBO.VertexNormalUV, Vector2f, Matrix4f, UInt, DrawGroup>.bindVBO(
                vbo: VBO.VertexNormalUV,
            ) =
                Builder<Matrix4f, Int, Int, VBO.VertexNormalUV, Nothing, Nothing, Nothing, Nothing>(
                    viewMatrix, projectionMatrix, material, textureName,
                    Optional.of(vbo)
                )

            @JvmName("setLightMapCoords2")
            fun Builder<Matrix4f, Int, Int, VBO.VertexNormalUV, Vector2f, Matrix4f, UInt, DrawGroup>.setLightMapCoords(
                uv: Vector2f,
            ) =
                Builder<Matrix4f, Int, Int, VBO.VertexNormalUV, Vector2f, Nothing, Nothing, Nothing>(
                    viewMatrix, projectionMatrix, material, textureName, vbo,
                    Optional.of(uv)
                )

            @JvmName("setModelMatrix2")
            fun Builder<Matrix4f, Int, Int, VBO.VertexNormalUV, Vector2f, Matrix4f, UInt, DrawGroup>.setModelMatrix(
                modelMatrix: Matrix4f,
            ) =
                Builder<Matrix4f, Int, Int, VBO.VertexNormalUV, Vector2f, Matrix4f, Nothing, Nothing>(
                    viewMatrix, projectionMatrix, material, textureName, vbo, lightMapUV,
                    Optional.of(Matrix4f(modelMatrix))
                )

            @JvmName("setColor3")
            fun Builder<Matrix4f, Int, Int, VBO.VertexNormalUV, Vector2f, Matrix4f, UInt, DrawGroup>.setColor(
                color: UInt,
            ) =
                Builder<Matrix4f, Int, Int, VBO.VertexNormalUV, Vector2f, Matrix4f, UInt, Nothing>(
                    viewMatrix, projectionMatrix, material, textureName, vbo, lightMapUV, modelMatrix,
                    Optional.of(color)
                )

            @JvmName("useModel2")
            fun Builder<Matrix4f, Int, Int, VBO.VertexNormalUV, Vector2f, Matrix4f, UInt, DrawGroup>.useModel(
                model: DrawGroup,
            ) =
                Builder(
                    viewMatrix, projectionMatrix, material, textureName, vbo, lightMapUV, modelMatrix, color,
                    Optional.of(model)
                )
        }
    }
}
