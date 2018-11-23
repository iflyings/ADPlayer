package com.android.iflyings.player.model

import android.graphics.Rect
import android.opengl.Matrix
import java.nio.ByteBuffer
import java.nio.ByteOrder

class MediaModel {

    private val mScaleType = ScaleType.FitXY

    val shaderVector = floatArrayOf(1.0f, 1.0f, 1.0f, 1.0f)
    var shaderType: Int = 0

    val positionMatrix = FloatArray(16)
    val textureMatrix = FloatArray(16)
    val vertexBuffer = ByteBuffer.allocateDirect(12 * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()!!
    val textureBuffer = ByteBuffer.allocateDirect(8 * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()!!
    val drawBuffer = ByteBuffer.allocateDirect(6 * 2)
            .order(ByteOrder.nativeOrder())
            .asShortBuffer()!!

    init {
        drawBuffer.put(shortArrayOf(0, 1, 2, 0, 2, 3)).position(0)
        Matrix.setIdentityM(positionMatrix, 0)
        Matrix.setIdentityM(textureMatrix, 0)
        reset()
    }

    fun notifyMediaModelUpdated(windowData: WindowData, textureData: TextureData) {
        if (windowData.isAvailable() && textureData.isAvailable()) {
            mScaleType.update(this, windowData, textureData)
        }
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
    }
    fun hide() {
        shaderType = 1
    }
    fun setAlpha(alpha: Float) {
        shaderType = 2
        shaderVector[3] = alpha
    }
    fun setCircle(ratio: Float, windowData: WindowData) {
        shaderType = 3
        shaderVector[0] = windowData.centerX
        shaderVector[1] = windowData.centerY
        shaderVector[2] = ratio * windowData.radius
        shaderVector[3] = 1.0f
    }
    fun setMosaic(ratio: Float, textureData: TextureData) {
        shaderType = 4
        shaderVector[0] = textureData.width.toFloat()
        shaderVector[1] = textureData.height.toFloat()
        shaderVector[2] = 100 * ratio
        shaderVector[3] = 1.0f
    }
    fun setRect(x: Float, y: Float, windowData: WindowData) {
        shaderType = 5
        shaderVector[0] = windowData.left.toFloat()
        shaderVector[1] = windowData.screenHeight.toFloat() - windowData.bottom.toFloat() + windowData.height * (1 - y)
        shaderVector[2] = windowData.left.toFloat() + windowData.width * x
        shaderVector[3] = windowData.screenHeight - windowData.top.toFloat()
    }
    fun setShutter(ratio: Float, windowData: WindowData) {
        shaderType = 6
        shaderVector[0] = windowData.screenHeight.toFloat() - windowData.bottom.toFloat()
        shaderVector[1] = windowData.left.toFloat()
        shaderVector[2] = 100.0f
        shaderVector[3] = 100.0f * ratio
    }
    fun setBright(l: Float) {
        shaderType = 7
        shaderVector[0] = l
    }
    fun setThreshold(r: Float, g: Float, b: Float, enabled: Boolean) {
        shaderType = 8
        shaderVector[0] = r
        shaderVector[1] = g
        shaderVector[2] = b
        shaderVector[3] = if (enabled) 1.0f else 0.0f
    }

    sealed class ScaleType {

        object Center: ScaleType() {
            override fun update(mediaModel: MediaModel, windowData: WindowData, textureData: TextureData) {
                val rect = Rect(windowData.left, windowData.top, windowData.right, windowData.bottom)
                if (1.0f * windowData.width / windowData.height >= 1.0f * textureData.width / textureData.height) {
                    val winWidth = (1.0f * windowData.height * textureData.width / textureData.height).toInt()
                    val spanWidth = (windowData.width - winWidth) / 2
                    rect.left = windowData.left + spanWidth
                    rect.right = windowData.right - spanWidth
                } else {
                    val winHeight = (1.0 * windowData.width * textureData.height / textureData.width).toInt()
                    val spanHeight = (windowData.height - winHeight) / 2
                    rect.top = windowData.top + spanHeight
                    rect.bottom = windowData.bottom - spanHeight
                }
                //Log.i("zw","CenterInside ${rect.left},${rect.top},${rect.right},${rect.bottom}")
                mediaModel.vertexBuffer.put(windowData.getVertexBuffer(rect)).position(0)
                mediaModel.textureBuffer.put(textureData.getTextureBuffer(null)).position(0)
            }
        }
        object CenterInside: ScaleType() {
            override fun update(mediaModel: MediaModel, windowData: WindowData, textureData: TextureData) {
                val rect = Rect(windowData.left, windowData.top, windowData.right, windowData.bottom)
                if (windowData.width >= textureData.width && windowData.height >= textureData.height) {
                    val spanWidth = (windowData.width - textureData.width) / 2
                    val spanHeight = (windowData.height - textureData.height) / 2
                    rect.left = windowData.left + spanWidth
                    rect.right = windowData.right - spanWidth
                    rect.top = windowData.top + spanHeight
                    rect.bottom = windowData.bottom - spanHeight
                } else {
                    if (1.0f * windowData.width / windowData.height >= 1.0f * textureData.width / textureData.height) {
                        val winWidth = (1.0f * windowData.height * textureData.width / textureData.height).toInt()
                        val spanWidth = (windowData.width - winWidth) / 2
                        rect.left = windowData.left + spanWidth
                        rect.right = windowData.right - spanWidth
                    } else {
                        val winHeight = (1.0 * windowData.width * textureData.height / textureData.width).toInt()
                        val spanHeight = (windowData.height - winHeight) / 2
                        rect.top = windowData.top + spanHeight
                        rect.bottom = windowData.bottom - spanHeight
                    }
                }
                //Log.i("zw","CenterInside ${rect.left},${rect.top},${rect.right},${rect.bottom}")
                mediaModel.vertexBuffer.put(windowData.getVertexBuffer(rect)).position(0)
                mediaModel.textureBuffer.put(textureData.getTextureBuffer(null)).position(0)
            }
        }

        object FitXY: ScaleType() {
            override fun update(mediaModel: MediaModel, windowData: WindowData, textureData: TextureData) {
                mediaModel.vertexBuffer.put(windowData.getVertexBuffer(null)).position(0)
                mediaModel.textureBuffer.put(textureData.getTextureBuffer(null)).position(0)
            }
        }

        object FitStart: ScaleType() {
            override fun update(mediaModel: MediaModel, windowData: WindowData, textureData: TextureData) {
                val rect = Rect(windowData.left, windowData.top, windowData.right, windowData.bottom)
                if (windowData.width >= textureData.width && windowData.height >= textureData.height) {
                    rect.right = windowData.left + textureData.width
                    rect.bottom = windowData.top + textureData.height
                } else {
                    if (1.0f * windowData.width / windowData.height >= 1.0f * textureData.width / textureData.height) {
                        val winWidth = 1.0f * windowData.height * textureData.width / textureData.height
                        rect.bottom = windowData.bottom
                        rect.right = windowData.left + winWidth.toInt()
                    } else {
                        val winHeight = 1.0 * windowData.width * textureData.height / textureData.width
                        rect.right = windowData.right
                        rect.bottom = windowData.top + winHeight.toInt()
                    }
                }
                mediaModel.vertexBuffer.put(windowData.getVertexBuffer(rect)).position(0)
                mediaModel.textureBuffer.put(textureData.getTextureBuffer(null)).position(0)
            }
        }

        abstract fun update(mediaModel: MediaModel, windowData: WindowData, textureData: TextureData)

    }
}