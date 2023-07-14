package com.github.kotatsu_rtm.kotatsulib.api.renderer

import org.joml.Matrix4f
import org.joml.Vector2f

interface Renderer<T : Any> {
    fun render(
        target: T?,
        tickProgression: Float,
        modelMatrix: Matrix4f, viewMatrix: Matrix4f, projectionMatrix: Matrix4f,
        lightMapCoordinate: Vector2f
    )
}
