package com.android.iflyings.player.model

import android.graphics.Rect
import android.opengl.Matrix
import com.android.iflyings.player.info.TextureInfo
import com.android.iflyings.player.info.WindowInfo
import java.nio.ByteBuffer
import java.nio.ByteOrder

class MediaModel(wininfo: WindowInfo, texInfo: TextureInfo) {

    val windowInfo = wininfo
    private val textureInfo = texInfo

    private val mScaleType = ScaleType.Center

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

    fun notifyMediaModelUpdated() {
        mScaleType.update(this)
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
    fun setAlpha(alpha: Float) {
        shaderType = 1
        shaderVector[3] = alpha
    }
    fun setCircle(ratio: Float) {
        shaderType = 2
        shaderVector[0] = windowInfo.centerX
        shaderVector[1] = windowInfo.centerY
        shaderVector[2] = ratio * windowInfo.radius
        shaderVector[3] = 1.0f
    }
    fun setMosaic(ratio: Float) {
        shaderType = 3
        shaderVector[0] = textureInfo.width.toFloat()
        shaderVector[1] = textureInfo.height.toFloat()
        shaderVector[2] = 100 * ratio
        shaderVector[3] = 1.0f
    }
    fun setRect(x: Float, y: Float) {
        shaderType = 4
        shaderVector[0] = windowInfo.left.toFloat()
        shaderVector[1] = windowInfo.screenHeight.toFloat() - windowInfo.bottom.toFloat() + windowInfo.height * (1 - y)
        shaderVector[2] = windowInfo.left.toFloat() + windowInfo.width * x
        shaderVector[3] = windowInfo.screenHeight - windowInfo.top.toFloat()
    }
    fun setShutter(ratio: Float) {
        shaderType = 5
        shaderVector[0] = windowInfo.screenHeight.toFloat() - windowInfo.bottom.toFloat()
        shaderVector[1] = windowInfo.left.toFloat()
        shaderVector[2] = 100.0f
        shaderVector[3] = 100.0f * ratio
    }
    fun setThreshold(r: Int, g: Int, b: Int, enabled: Boolean) {
        shaderType = 6
        shaderVector[0] = r.toFloat()
        shaderVector[1] = g.toFloat()
        shaderVector[2] = b.toFloat()
        shaderVector[3] = if (enabled) 1.0f else 0.0f
    }

    sealed class ScaleType {

        object Center : ScaleType()
        object CenterInside : ScaleType()
        object FitXY : ScaleType()
        object FitStart : ScaleType()

        private fun updateFitXY(mediaModel: MediaModel) {
            mediaModel.vertexBuffer.put(mediaModel.windowInfo.getVertexBuffer(null)).position(0)
            mediaModel.textureBuffer.put(mediaModel.textureInfo.getTextureBuffer(null)).position(0)
        }
        private fun updateFitStart(mediaModel: MediaModel) {
            val wininfo = mediaModel.windowInfo
            val texInfo = mediaModel.textureInfo
            val rect = Rect(wininfo.left, wininfo.top, wininfo.right, wininfo.bottom)
            if (wininfo.width >= texInfo.width && wininfo.height >= texInfo.height) {
                rect.right = wininfo.left + texInfo.width
                rect.bottom = wininfo.top + texInfo.height
            } else {
                if (1.0f * wininfo.width / wininfo.height >= 1.0f * texInfo.width / texInfo.height) {
                    val winWidth = 1.0f * wininfo.height * texInfo.width / texInfo.height
                    rect.bottom = wininfo.bottom
                    rect.right = wininfo.left + winWidth.toInt()
                } else {
                    val winHeight = 1.0 * wininfo.width * texInfo.height / texInfo.width
                    rect.right = wininfo.right
                    rect.bottom = wininfo.top + winHeight.toInt()
                }
            }
            mediaModel.vertexBuffer.put(mediaModel.windowInfo.getVertexBuffer(rect)).position(0)
            mediaModel.textureBuffer.put(mediaModel.textureInfo.getTextureBuffer(null)).position(0)
        }
        private fun updateCenterInside(mediaModel: MediaModel) {
            val wininfo = mediaModel.windowInfo
            val texInfo = mediaModel.textureInfo
            val rect = Rect(wininfo.left, wininfo.top, wininfo.right, wininfo.bottom)
            if (wininfo.width >= texInfo.width && wininfo.height >= texInfo.height) {
                val spanWidth = (wininfo.width - texInfo.width) / 2
                val spanHeight = (wininfo.height - texInfo.height) / 2
                rect.left = wininfo.left + spanWidth
                rect.right = wininfo.right - spanWidth
                rect.top = wininfo.top + spanHeight
                rect.bottom = wininfo.bottom - spanHeight
            } else {
                if (1.0f * wininfo.width / wininfo.height >= 1.0f * texInfo.width / texInfo.height) {
                    val winWidth = (1.0f * wininfo.height * texInfo.width / texInfo.height).toInt()
                    val spanWidth = (wininfo.width - winWidth) / 2
                    rect.left = wininfo.left + spanWidth
                    rect.right = wininfo.right - spanWidth
                } else {
                    val winHeight = (1.0 * wininfo.width * texInfo.height / texInfo.width).toInt()
                    val spanHeight = (wininfo.height - winHeight) / 2
                    rect.top = wininfo.top + spanHeight
                    rect.bottom = wininfo.bottom - spanHeight
                }
            }
            //Log.i("zw","CenterInside ${rect.left},${rect.top},${rect.right},${rect.bottom}")
            mediaModel.vertexBuffer.put(mediaModel.windowInfo.getVertexBuffer(rect)).position(0)
            mediaModel.textureBuffer.put(mediaModel.textureInfo.getTextureBuffer(null)).position(0)
        }
        private fun updateCenter(mediaModel: MediaModel) {
            val wininfo = mediaModel.windowInfo
            val texInfo = mediaModel.textureInfo
            val rect = Rect(wininfo.left, wininfo.top, wininfo.right, wininfo.bottom)
            if (1.0f * wininfo.width / wininfo.height >= 1.0f * texInfo.width / texInfo.height) {
                val winWidth = (1.0f * wininfo.height * texInfo.width / texInfo.height).toInt()
                val spanWidth = (wininfo.width - winWidth) / 2
                rect.left = wininfo.left + spanWidth
                rect.right = wininfo.right - spanWidth
            } else {
                val winHeight = (1.0 * wininfo.width * texInfo.height / texInfo.width).toInt()
                val spanHeight = (wininfo.height - winHeight) / 2
                rect.top = wininfo.top + spanHeight
                rect.bottom = wininfo.bottom - spanHeight
            }
            //Log.i("zw","CenterInside ${rect.left},${rect.top},${rect.right},${rect.bottom}")
            mediaModel.vertexBuffer.put(mediaModel.windowInfo.getVertexBuffer(rect)).position(0)
            mediaModel.textureBuffer.put(mediaModel.textureInfo.getTextureBuffer(null)).position(0)
        }

        fun update(mediaModel: MediaModel) {
            when (this) {
                FitXY -> {
                    updateFitXY(mediaModel)
                }
                FitStart -> {
                    updateFitStart(mediaModel)
                }
                Center -> {
                    updateCenter(mediaModel)
                }
                CenterInside -> {
                    updateCenterInside(mediaModel)
                }
            }
        }

    }
}