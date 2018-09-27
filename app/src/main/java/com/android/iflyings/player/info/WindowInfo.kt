package com.android.iflyings.player.info

import android.graphics.Rect

class WindowInfo {

    private var mWinRect: Rect? = null

    var screenWidth = 0
        private set
    var screenHeight = 0
        private set

    var radius: Float = 0.0f
        private set
    var centerX: Float = 0.0f
        private set
    var centerY: Float = 0.0f
        private set

    val left: Int
        get() = if (mWinRect != null) mWinRect!!.left else 0

    val top: Int
        get() = if (mWinRect != null) mWinRect!!.top else 0

    val right: Int
        get() = if (mWinRect != null) mWinRect!!.right else screenWidth

    val bottom: Int
        get() = if (mWinRect != null) mWinRect!!.bottom else screenHeight

    val width: Int
        get() = if (mWinRect != null) mWinRect!!.width() else screenWidth

    val height: Int
        get() = if (mWinRect != null) mWinRect!!.height() else screenHeight

    fun setScreenSize(w: Int, h: Int) {
        screenWidth = w
        screenHeight = h
        update(mWinRect, w, h)
    }

    fun setWindowSize(rect: Rect?) {
        mWinRect = rect
        update(rect, screenWidth, screenHeight)
    }

    private fun update(rect: Rect?, width: Int, height: Int) {
        if (rect != null) {
            val w = rect.width().toDouble()
            val h = rect.height().toDouble()
            radius = (Math.sqrt(w * w + h * h) / 2.0f).toFloat()
            centerX = rect.centerX().toFloat()
            centerY = rect.centerY().toFloat()
        } else {
            val w = width.toDouble()
            val h = height.toDouble()
            radius = (Math.sqrt(w * w + h * h) / 2.0f).toFloat()
            centerX = width / 2.0f
            centerY = height / 2.0f
        }
    }

    fun getVertexBuffer(rect: Rect?): FloatArray {
        val vertexs = FloatArray(12)
        if (rect != null) {
            vertexs[0] = 2.0f * rect.left / screenWidth - 1
            vertexs[1] = 1 - 2.0f * rect.top / screenHeight
            vertexs[2] = 0.0f
            vertexs[3] = 2.0f * rect.left / screenWidth - 1
            vertexs[4] = 1 - 2.0f * rect.bottom / screenHeight
            vertexs[5] = 0.0f
            vertexs[6] = 2.0f * rect.right / screenWidth - 1
            vertexs[7] = 1 - 2.0f * rect.bottom / screenHeight
            vertexs[8] = 0.0f
            vertexs[9] = 2.0f * rect.right / screenWidth - 1
            vertexs[10] = 1 - 2.0f * rect.top / screenHeight
            vertexs[11] = 0.0f
        } else {
            vertexs[0] = 2.0f * left / screenWidth - 1
            vertexs[1] = 1 - 2.0f * top / screenHeight
            vertexs[2] = 0.0f
            vertexs[3] = 2.0f * left / screenWidth - 1
            vertexs[4] = 1 - 2.0f * bottom / screenHeight
            vertexs[5] = 0.0f
            vertexs[6] = 2.0f * right / screenWidth - 1
            vertexs[7] = 1 - 2.0f * bottom / screenHeight
            vertexs[8] = 0.0f
            vertexs[9] = 2.0f * right / screenWidth - 1
            vertexs[10] = 1 - 2.0f * top / screenHeight
            vertexs[11] = 0.0f
        }
        return vertexs
    }
}