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
object ProgressBarShader : Shader<ProgressBarShader.RenderData>(
    ProgressBarShader::class.java.classLoader
        .getResourceAsStream("shader/progress_bar.vsh")!!
        .readBytes().decodeToString(),
    ProgressBarShader::class.java.classLoader
        .getResourceAsStream("shader/progress_bar.fsh")!!
        .readBytes().decodeToString()
) {
    //uniform
    private const val MODEL_VIEW_PROJECTION_MATRIX_LOCATION = 0
    private const val MOST_COLOR_LOCATION = 10
    private const val LEAST_COLOR_LOCATION = 11
    private const val PROGRESSION_LOCATION = 12
    private const val LIGHT_SAMPLER_LOCATION = 13
    private const val LIGHT_POSITION_LOCATION = 14
    private const val SHOULD_NOT_LIGHTING_LOCATION = 15
    private const val INVERSE_MODEL_MATRIX_LOCATION = 16

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
        var shouldNotLighting: Boolean by InvokeBlockOnChange {
            GL20.glUniform1f(SHOULD_NOT_LIGHTING_LOCATION, if (it) 1.0f else 0.0f)
        }
        var mostColor: UInt by InvokeBlockOnChange {
            GL20.glUniform4f(
                MOST_COLOR_LOCATION,
                (it shr 24).toFloat() / 255.0F,
                (it shr 16 and 0xffu).toFloat() / 255.0F,
                (it shr 8 and 0xffu).toFloat() / 255.0F,
                (it and 0xffu).toFloat() / 255.0F
            )
        }
        var leastColor: UInt by InvokeBlockOnChange {
            GL20.glUniform4f(
                LEAST_COLOR_LOCATION,
                (it shr 24).toFloat() / 255.0F,
                (it shr 16 and 0xffu).toFloat() / 255.0F,
                (it shr 8 and 0xffu).toFloat() / 255.0F,
                (it and 0xffu).toFloat() / 255.0F
            )
        }
        var progression: Float by InvokeBlockOnChange { GL20.glUniform1f(PROGRESSION_LOCATION, it) }

        callBuffer.forEach {
            modelViewProjectionMatrix = it.modelViewProjectionMatrix
            inverseModelMatrix = it.inverseModelMatrix
            vbo = it.vbo
            ibo = it.ibo
            lightMapUV = it.lightMapUV
            shouldNotLighting = it.disableLighting
            mostColor = it.mostColor
            leastColor = it.leastColor
            progression = it.progression

            GL11.glDrawElements(
                GL11.GL_TRIANGLES,
                it.iboInfoToDraw.size,
                GL11.GL_UNSIGNED_INT,
                it.iboInfoToDraw.offset.toLong() * Int.SIZE_BYTES
            )
        }

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
        val ibo: IndexBufferObject,
        val iboInfoToDraw: IboInfo,
        val mostColor: UInt,
        val leastColor: UInt,
        val progression: Float,
        val disableLighting: Boolean,
    ) : dev.siro256.forgelib.rtm_glsl.shader.Shader.RenderData

    data class Builder<A : Any, B : Any, C : Any, D : Any, E : Any, F : Any, G : Any, H : Any>(
        private val viewMatrix: Optional<A> = Optional.empty(),
        private val projectionMatrix: Optional<A> = Optional.empty(),
        private val material: Optional<B> = Optional.empty(),
        private val vbo: Optional<C> = Optional.empty(),
        private val lightMapUV: Optional<D> = Optional.empty(),
        private val modelMatrix: Optional<E> = Optional.empty(),
        private val mostColor: Optional<F> = Optional.empty(),
        private val leastColor: Optional<F> = Optional.empty(),
        private val progression: Optional<G> = Optional.empty(),
        private val model: Optional<H> = Optional.empty(),
    ) {
        companion object {
            fun Builder<Matrix4f, Nothing, Nothing, Nothing, Nothing, Nothing, Nothing, Nothing>.setMaterial(id: Int) =
                Builder<Matrix4f, Int, Nothing, Nothing, Nothing, Nothing, Nothing, Nothing>(
                    viewMatrix, projectionMatrix,
                    Optional.of(id)
                )

            fun Builder<Matrix4f, Int, Nothing, Nothing, Nothing, Nothing, Nothing, Nothing>.bindVBO(
                vbo: VBO.VertexNormalUV,
            ) =
                Builder<Matrix4f, Int, VBO.VertexNormalUV, Nothing, Nothing, Nothing, Nothing, Nothing>(
                    viewMatrix, projectionMatrix, material,
                    Optional.of(vbo)
                )

            fun Builder<Matrix4f, Int, VBO.VertexNormalUV, Nothing, Nothing, Nothing, Nothing, Nothing>.setLightMapCoords(
                uv: Vector2f,
            ) =
                Builder<Matrix4f, Int, VBO.VertexNormalUV, Vector2f, Nothing, Nothing, Nothing, Nothing>(
                    viewMatrix, projectionMatrix, material, vbo,
                    Optional.of(uv)
                )

            fun Builder<Matrix4f, Int, VBO.VertexNormalUV, Vector2f, Nothing, Nothing, Nothing, Nothing>.setModelMatrix(
                modelMatrix: Matrix4f,
            ) =
                Builder<Matrix4f, Int, VBO.VertexNormalUV, Vector2f, Matrix4f, Nothing, Nothing, Nothing>(
                    viewMatrix, projectionMatrix, material, vbo, lightMapUV,
                    Optional.of(Matrix4f(modelMatrix))
                )

            fun Builder<Matrix4f, Int, VBO.VertexNormalUV, Vector2f, Matrix4f, Nothing, Nothing, Nothing>.setColor(
                most: UInt,
                least: UInt,
            ) =
                Builder<Matrix4f, Int, VBO.VertexNormalUV, Vector2f, Matrix4f, UInt, Nothing, Nothing>(
                    viewMatrix, projectionMatrix, material, vbo, lightMapUV, modelMatrix,
                    Optional.of(most), Optional.of(least)
                )

            fun Builder<Matrix4f, Int, VBO.VertexNormalUV, Vector2f, Matrix4f, UInt, Nothing, Nothing>.setProgression(
                progression: Float,
            ) =
                Builder<Matrix4f, Int, VBO.VertexNormalUV, Vector2f, Matrix4f, UInt, Float, Nothing>(
                    viewMatrix, projectionMatrix, material, vbo, lightMapUV, modelMatrix, mostColor, leastColor,
                    Optional.of(progression)
                )

            fun Builder<Matrix4f, Int, VBO.VertexNormalUV, Vector2f, Matrix4f, UInt, Float, Nothing>.useModel(
                model: DrawGroup,
            ) =
                Builder(
                    viewMatrix, projectionMatrix,
                    material,
                    vbo,
                    lightMapUV,
                    modelMatrix,
                    mostColor, leastColor,
                    progression,
                    Optional.of(model)
                )

            fun Builder<Matrix4f, Int, VBO.VertexNormalUV, Vector2f, Matrix4f, UInt, Float, DrawGroup>.render(
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
                            modelMatrix,
                            lightMapUV.get(),
                            vbo.get(),
                            model.ibo,
                            indicesInfo,
                            mostColor.get(),
                            leastColor.get(),
                            progression.get(),
                            disableLighting
                        )
                    )
                }

            @JvmName("bindVBO2")
            fun Builder<Matrix4f, Int, VBO.VertexNormalUV, Vector2f, Matrix4f, UInt, Float, DrawGroup>.bindVBO(
                vbo: VBO.VertexNormalUV,
            ) =
                Builder<Matrix4f, Int, VBO.VertexNormalUV, Nothing, Nothing, Nothing, Nothing, Nothing>(
                    viewMatrix, projectionMatrix, material,
                    Optional.of(vbo)
                )

            @JvmName("setLightMapCoords2")
            fun Builder<Matrix4f, Int, VBO.VertexNormalUV, Vector2f, Matrix4f, UInt, Float, DrawGroup>.setLightMapCoords(
                uv: Vector2f,
            ) =
                Builder<Matrix4f, Int, VBO.VertexNormalUV, Vector2f, Nothing, Nothing, Nothing, Nothing>(
                    viewMatrix, projectionMatrix, material, vbo,
                    Optional.of(uv)
                )

            @JvmName("setModelMatrix2")
            fun Builder<Matrix4f, Int, VBO.VertexNormalUV, Vector2f, Matrix4f, UInt, Float, DrawGroup>.setModelMatrix(
                modelMatrix: Matrix4f,
            ) =
                Builder<Matrix4f, Int, VBO.VertexNormalUV, Vector2f, Matrix4f, Nothing, Nothing, Nothing>(
                    viewMatrix, projectionMatrix, material, vbo, lightMapUV,
                    Optional.of(Matrix4f(modelMatrix))
                )

            @JvmName("setColor2")
            fun Builder<Matrix4f, Int, VBO.VertexNormalUV, Vector2f, Matrix4f, UInt, Float, DrawGroup>.setColor(
                most: UInt,
                least: UInt,
            ) =
                Builder<Matrix4f, Int, VBO.VertexNormalUV, Vector2f, Matrix4f, UInt, Nothing, Nothing>(
                    viewMatrix, projectionMatrix, material, vbo, lightMapUV, modelMatrix,
                    Optional.of(most), Optional.of(least)
                )


            @JvmName("setProgression2")
            fun Builder<Matrix4f, Int, VBO.VertexNormalUV, Vector2f, Matrix4f, UInt, Float, DrawGroup>.setProgression(
                progression: Float,
            ) =
                Builder<Matrix4f, Int, VBO.VertexNormalUV, Vector2f, Matrix4f, UInt, Float, Nothing>(
                    viewMatrix, projectionMatrix, material, vbo, lightMapUV, modelMatrix, mostColor, leastColor,
                    Optional.of(progression)
                )

            @JvmName("useModel2")
            fun Builder<Matrix4f, Int, VBO.VertexNormalUV, Vector2f, Matrix4f, UInt, Float, DrawGroup>.useModel(
                model: DrawGroup,
            ) =
                Builder(
                    viewMatrix, projectionMatrix,
                    material,
                    vbo,
                    lightMapUV,
                    modelMatrix,
                    mostColor, leastColor,
                    progression,
                    Optional.of(model)
                )
        }
    }
}
