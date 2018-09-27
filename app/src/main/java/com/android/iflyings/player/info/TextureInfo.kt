package com.android.iflyings.player.info

import android.graphics.Rect

class TextureInfo {
    private var mTexRect: Rect? = null
    var texWidth = 0
        private set
    var texHeight = 0
        private set

    val left: Int
        get() = if (mTexRect != null) mTexRect!!.left else 0

    val top: Int
        get() = if (mTexRect != null) mTexRect!!.top else 0

    val right: Int
        get() = if (mTexRect != null) mTexRect!!.right else texWidth

    val bottom: Int
        get() = if (mTexRect != null) mTexRect!!.bottom else texHeight

    val width: Int
        get() = if (mTexRect != null) mTexRect!!.width() else texWidth

    val height: Int
        get() = if (mTexRect != null) mTexRect!!.height() else texHeight

    fun setTextureSize(tWidth: Int, tHeight: Int) {
        texWidth = tWidth
        texHeight = tHeight
    }

    fun setTextureShow(rect: Rect?) {
        mTexRect = rect
    }

    fun getTextureBuffer(rect: Rect?): FloatArray {
        val textures = FloatArray(8)
        if (rect != null) {
            textures[0] = 1.0f * rect.left / texWidth
            textures[1] = 1.0f * rect.top / texHeight
            textures[2] = 1.0f * rect.left / texWidth
            textures[3] = 1.0f * rect.bottom / texHeight
            textures[4] = 1.0f * rect.right / texWidth
            textures[5] = 1.0f * rect.bottom / texHeight
            textures[6] = 1.0f * rect.right / texWidth
            textures[7] = 1.0f * rect.top / texHeight
        } else {
            textures[0] = 1.0f * left / texWidth
            textures[1] = 1.0f * top / texHeight
            textures[2] = 1.0f * left / texWidth
            textures[3] = 1.0f * bottom / texHeight
            textures[4] = 1.0f * right / texWidth
            textures[5] = 1.0f * bottom / texHeight
            textures[6] = 1.0f * right / texWidth
            textures[7] = 1.0f * top / texHeight
        }
        return textures
    }
}