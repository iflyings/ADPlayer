package com.android.iflyings.player.model

import android.opengl.Matrix
import java.nio.ByteBuffer
import java.nio.ByteOrder

class FrameModel {
    val shaderVector = floatArrayOf(1.0f, 1.0f, 1.0f, 1.0f)
    var shaderType: Int = 0

    val positionMatrix = FloatArray(16)
    val textureMatrix = FloatArray(16)
    val vertexBuffer = ByteBuffer.allocateDirect(12 * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(floatArrayOf(-1.0f, 1.0f, 0.0f, -1.0f, -1.0f, 0.0f, 1.0f, -1.0f, 0.0f, 1.0f, 1.0f, 0.0f)).position(0)!!
    val textureBuffer = ByteBuffer.allocateDirect(8 * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(floatArrayOf(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f)).position(0)!!
    val drawBuffer = ByteBuffer.allocateDirect(6 * 2)
            .order(ByteOrder.nativeOrder())
            .asShortBuffer()
            .put(shortArrayOf(0, 1, 2, 0, 2, 3)).position(0)!!

    init {
        shaderType = 0
        Matrix.setIdentityM(positionMatrix, 0)
        Matrix.setIdentityM(textureMatrix, 0)
    }

    fun setScale(x: Float, y: Float, z: Float) {
        Matrix.scaleM(positionMatrix, 0, x, y, z)
    }

    fun setRotate(a: Float, x: Float, y: Float, z: Float) {
        Matrix.rotateM(positionMatrix, 0, a, x, y, z)
    }

    fun reset() {
        shaderType = 0
        Matrix.setIdentityM(positionMatrix, 0)
        Matrix.setIdentityM(textureMatrix, 0)
    }
    fun setHDR() {
        shaderType = 1
        shaderVector[0] = 1.1f
    }
    fun setGrayPhoto() {
        shaderType = 2
    }
    fun setOldPhoto() {
        shaderType = 3
    }
    fun setEmboss(texWidth: Int, texHeight: Int) {
        shaderType = 4
        shaderVector[0] = texWidth.toFloat()
        shaderVector[1] = texHeight.toFloat()
    }
    fun setShake(distance: Float) {
        shaderType = 5
        shaderVector[0] = distance
    }
}