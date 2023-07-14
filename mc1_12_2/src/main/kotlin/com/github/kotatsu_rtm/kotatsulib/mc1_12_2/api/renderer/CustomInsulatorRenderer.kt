package com.github.kotatsu_rtm.kotatsulib.mc1_12_2.api.renderer

import jp.ngt.rtm.electric.TileEntityInsulator
import org.joml.Matrix4f

abstract class CustomInsulatorRenderer<T : TileEntityInsulator> : TileEntityRenderer<T>() {
    final override fun getModelOffset(tileEntity: T?): Matrix4f {
        val offset = Matrix4f()
        tileEntity?.resourceState?.resourceSet?.config?.offset?.let { offset.translate(it[0], it[1], it[2]) }
        return offset
    }
}
