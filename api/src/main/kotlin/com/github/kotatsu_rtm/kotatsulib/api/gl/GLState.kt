package com.github.kotatsu_rtm.kotatsulib.api.gl

import org.joml.Matrix4f

interface GLState {
    fun getModelView(): Matrix4f

    fun getProjection(): Matrix4f
}
