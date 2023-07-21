package com.github.kotatsu_rtm.kotatsulib.mc1_12_2.api.renderer

import com.github.kotatsu_rtm.kotatsulib.api.toRadians
import com.github.kotatsu_rtm.kotatsulib.mc1_12_2.api.gl.GLStateImpl
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import org.joml.Matrix4f

abstract class TileEntityRenderer<T : TileEntity> : RendererBase<T>, TileEntitySpecialRenderer<T>() {
    @Suppress("WRONG_NULLABILITY_FOR_JAVA_OVERRIDE")
    override fun render(
        tileEntity: T?,
        x: Double, y: Double, z: Double,
        tickProgression: Float,
        destroyStage: Int,
        alpha: Float
    ) {
        val modelMatrix = Matrix4f()
        modelMatrix.translate(x.toFloat() + 0.5F, y.toFloat() + 0.5F, z.toFloat() + 0.5F)
        modelMatrix.mul(getFixRTMOffset(tileEntity))

        render(
            tileEntity,
            tickProgression,
            modelMatrix, GLStateImpl.getView(), getProjectionMatrix(tileEntity),
            getLightMapCoordinate(tileEntity)
        )
    }

    abstract fun getModelOffset(tileEntity: T?): Matrix4f

    private fun getFixRTMOffset(tileEntity: T?): Matrix4f {
        val offset = Matrix4f()

        val nbt = tileEntity?.writeToNBT(NBTTagCompound()) ?: return offset

        if (nbt.hasKey("Yaw") && nbt.hasKey("offsetX") && nbt.hasKey("offsetY") && nbt.hasKey("offsetZ")) {
            offset.rotateY(nbt.getFloat("Yaw").toRadians())
            offset.translate(nbt.getFloat("offsetX"), nbt.getFloat("offsetY"), nbt.getFloat("offsetZ"))
        }

        return offset
    }
}
