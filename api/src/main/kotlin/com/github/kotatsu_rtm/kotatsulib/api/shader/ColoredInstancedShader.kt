package com.github.kotatsu_rtm.kotatsulib.api.shader

import com.github.kotatsu_rtm.kotatsulib.api.gl.VBO
import com.github.kotatsu_rtm.kotatsulib.api.kotlinutil.InvokeBlockOnChange
import com.github.kotatsu_rtm.kotatsulib.api.model.IboInfo
import dev.siro256.forgelib.rtm_glsl.BufferAllocator
import dev.siro256.forgelib.rtm_glsl.enum.GLBufferUsage
import dev.siro256.forgelib.rtm_glsl.wrapper.IndexBufferObject
import dev.siro256.forgelib.rtm_glsl.wrapper.VertexArrayObject
import dev.siro256.forgelib.rtm_glsl.wrapper.VertexBufferObject
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f
import org.lwjgl.opengl.*
import java.util.Optional

@Suppress("unused")
object ColoredInstancedShader : Shader<ColoredInstancedShader.RenderData>(
    ColoredInstancedShader::class.java.classLoader
        .getResourceAsStream("shader/colored_instanced.vsh")!!
        .readBytes().decodeToString(),
    ColoredInstancedShader::class.java.classLoader
        .getResourceAsStream("shader/colored_instanced.fsh")!!
        .readBytes().decodeToString()
) {
    //uniform
    private const val MODEL_VIEW_PROJECTION_MATRIX_LOCATION = 0
    private const val LIGHT_SAMPLER_LOCATION = 10
    private const val LIGHT_POSITION_LOCATION = 11
    private const val SHOULD_NOT_LIGHTING_LOCATION = 12

    //in
    private const val VERTEX_POSITION_LOCATION = 0
    private const val VERTEX_OFFSET_LOCATION = 1
    private const val COLOR_LOCATION = 2
    private const val NORMAL_LOCATION = 3

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

                GL43.glVertexAttribBinding(VERTEX_OFFSET_LOCATION, 1)
                GL43.glVertexAttribFormat(VERTEX_OFFSET_LOCATION, 3, GL11.GL_FLOAT, false, 0)
                GL20.glEnableVertexAttribArray(VERTEX_OFFSET_LOCATION)

                GL43.glVertexAttribBinding(COLOR_LOCATION, 1)
                GL43.glVertexAttribFormat(COLOR_LOCATION, 4, GL11.GL_FLOAT, true, 3 * Float.SIZE_BYTES)
                GL20.glEnableVertexAttribArray(COLOR_LOCATION)

                GL43.glVertexBindingDivisor(0, 0)
                GL43.glVertexBindingDivisor(1, 1)

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
        @Suppress("DuplicatedCode")
        var modelViewProjectionMatrix: Matrix4f by InvokeBlockOnChange {
            matrixBuffer.apply {
                rewind()
                it.get(this)
                rewind()
            }.let { buffer -> GL20.glUniformMatrix4(MODEL_VIEW_PROJECTION_MATRIX_LOCATION, false, buffer) }
        }
        var vbo: VBO.VertexNormalUV by InvokeBlockOnChange { it.bind(0) }
        var ibo: IndexBufferObject by InvokeBlockOnChange { it.bind() }
        var lightMapUV: Vector2f by InvokeBlockOnChange { GL20.glUniform2f(LIGHT_POSITION_LOCATION, it.x, it.y) }
        var shouldNotLighting: Boolean by InvokeBlockOnChange {
            GL20.glUniform1f(SHOULD_NOT_LIGHTING_LOCATION, if(it) 1.0f else 0.0f)
        }

        callBuffer.forEach {
            modelViewProjectionMatrix = it.modelViewProjectionMatrix
            vbo = it.vbo
            ibo = it.ibo
            lightMapUV = it.lightMapUV
            shouldNotLighting = it.disableLighting

            val instanceDataBuffer =
                object : VertexBufferObject(
                    GLBufferUsage.GL_DYNAMIC_DRAW,
                    it.instanceData.flatMap { data ->
                        listOf(
                            data.offset.x, data.offset.y, data.offset.z,
                            (data.rgba shr 24).toFloat() / 255.0F,
                            (data.rgba shr 16 and 0xffu).toFloat() / 255.0F,
                            (data.rgba shr 8 and 0xffu).toFloat() / 255.0F,
                            (data.rgba and 0xffu).toFloat() / 255.0F
                        )
                    }.toFloatArray(),
                    7 * Float.SIZE_BYTES
                ) {
                    fun delete() {
                        GL15.glDeleteBuffers(name)
                    }
                }

            instanceDataBuffer.bind(1)

            GL31.glDrawElementsInstanced(
                GL11.GL_TRIANGLES,
                it.iboInfoToDraw.size,
                GL11.GL_UNSIGNED_INT,
                it.iboInfoToDraw.offset.toLong() * Int.SIZE_BYTES,
                it.instanceData.size
            )

            instanceDataBuffer.delete()
        }

        callBuffer.clear()
    }

    fun updateProjection(matrix: Matrix4f) =
        Builder<Matrix4f, Nothing, Nothing, Nothing, Nothing, Nothing, Nothing>(Optional.of(matrix))

    data class RenderData(
        val modelViewProjectionMatrix: Matrix4f,
        val vbo: VBO.VertexNormalUV,
        val lightMapUV: Vector2f,
        val ibo: IndexBufferObject,
        val iboInfoToDraw: IboInfo,
        val instanceData: List<InstanceData>,
        val disableLighting: Boolean,
    ) : dev.siro256.forgelib.rtm_glsl.shader.Shader.RenderData

    data class InstanceData(val offset: Vector3f, val rgba: UInt)

    data class Builder<A : Any, B : Any, C : Any, D : Any, E : Any, F : Any, G : Any>(
        private val projectionMatrix: Optional<A> = Optional.empty(),
        private val vbo: Optional<B> = Optional.empty(),
        private val lightMapUV: Optional<C> = Optional.empty(),
        private val modelViewMatrix: Optional<D> = Optional.empty(),
        private val ibo: Optional<E> = Optional.empty(),
        private val iboInfoToDraw: Optional<F> = Optional.empty(),
        private val instanceData: Optional<G> = Optional.empty(),
    ) {
        companion object {
            fun Builder<Matrix4f, Nothing, Nothing, Nothing, Nothing, Nothing, Nothing>.bindVBO(
                vbo: VBO.VertexNormalUV,
            ) =
                Builder<Matrix4f, VBO.VertexNormalUV, Nothing, Nothing, Nothing, Nothing, Nothing>(
                    projectionMatrix,
                    Optional.of(vbo)
                )

            fun Builder<Matrix4f, VBO.VertexNormalUV, Nothing, Nothing, Nothing, Nothing, Nothing>.setLightMapCoords(
                uv: Vector2f,
            ) =
                Builder<Matrix4f, VBO.VertexNormalUV, Vector2f, Nothing, Nothing, Nothing, Nothing>(
                    projectionMatrix, vbo,
                    Optional.of(uv)
                )

            fun Builder<Matrix4f, VBO.VertexNormalUV, Vector2f, Nothing, Nothing, Nothing, Nothing>.setModelView(
                matrix: Matrix4f,
            ) =
                Builder<Matrix4f, VBO.VertexNormalUV, Vector2f, Matrix4f, Nothing, Nothing, Nothing>(
                    projectionMatrix, vbo, lightMapUV,
                    Optional.of(matrix)
                )

            fun Builder<Matrix4f, VBO.VertexNormalUV, Vector2f, Matrix4f, Nothing, Nothing, Nothing>.useModel(
                ibo: IndexBufferObject,
                iboInfoToDraw: IboInfo,
            ) =
                Builder<Matrix4f, VBO.VertexNormalUV, Vector2f, Matrix4f, IndexBufferObject, IboInfo, Nothing>(
                    projectionMatrix, vbo, lightMapUV, modelViewMatrix,
                    Optional.of(ibo),
                    Optional.of(iboInfoToDraw)
                )

            fun Builder<Matrix4f, VBO.VertexNormalUV, Vector2f, Matrix4f, IndexBufferObject, IboInfo, Nothing>.setInstanceData(
                instanceData: List<InstanceData>,
            ) =
                Builder(
                    projectionMatrix, vbo, lightMapUV, modelViewMatrix, ibo, iboInfoToDraw,
                    Optional.of(instanceData)
                )

            fun Builder<Matrix4f, VBO.VertexNormalUV, Vector2f, Matrix4f, IndexBufferObject, IboInfo, List<InstanceData>>.render(
                disableLighting: Boolean = false,
            ) =
                also {
                    val clonedProjectionMatrix = Matrix4f(projectionMatrix.get())
                    val modelViewProjectionMatrix = clonedProjectionMatrix.mul(modelViewMatrix.get())

                    callBuffer.add(
                        RenderData(
                            modelViewProjectionMatrix,
                            vbo.get(),
                            lightMapUV.get(),
                            ibo.get(),
                            iboInfoToDraw.get(),
                            instanceData.get(),
                            disableLighting
                        )
                    )
                }

            @JvmName("bindVBO2")
            fun Builder<Matrix4f, VBO.VertexNormalUV, Vector2f, Matrix4f, IndexBufferObject, IboInfo, List<InstanceData>>.bindVBO(
                vbo: VBO.VertexNormalUV,
            ) =
                Builder<Matrix4f, VBO.VertexNormalUV, Nothing, Nothing, Nothing, Nothing, Nothing>(
                    projectionMatrix,
                    Optional.of(vbo)
                )

            @JvmName("setLightMapCoords2")
            fun Builder<Matrix4f, VBO.VertexNormalUV, Vector2f, Matrix4f, IndexBufferObject, IboInfo, List<InstanceData>>.setLightMapCoords(
                uv: Vector2f,
            ) =
                Builder<Matrix4f, VBO.VertexNormalUV, Vector2f, Nothing, Nothing, Nothing, Nothing>(
                    projectionMatrix, vbo,
                    Optional.of(uv)
                )

            @JvmName("setModelView2")
            fun Builder<Matrix4f, VBO.VertexNormalUV, Vector2f, Matrix4f, IndexBufferObject, IboInfo, List<InstanceData>>.setModelView(
                matrix: Matrix4f,
            ) =
                Builder<Matrix4f, VBO.VertexNormalUV, Vector2f, Matrix4f, Nothing, Nothing, Nothing>(
                    projectionMatrix, vbo, lightMapUV,
                    Optional.of(matrix)
                )

            @JvmName("useModel2")
            fun Builder<Matrix4f, VBO.VertexNormalUV, Vector2f, Matrix4f, IndexBufferObject, IboInfo, List<InstanceData>>.useModel(
                ibo: IndexBufferObject,
                iboInfoToDraw: IboInfo,
            ) =
                Builder<Matrix4f, VBO.VertexNormalUV, Vector2f, Matrix4f, IndexBufferObject, IboInfo, Nothing>(
                    projectionMatrix, vbo, lightMapUV, modelViewMatrix,
                    Optional.of(ibo),
                    Optional.of(iboInfoToDraw)
                )
        }
    }
}
