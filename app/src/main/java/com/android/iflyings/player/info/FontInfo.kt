package com.android.iflyings.player.info

import android.graphics.Rect
import com.android.iflyings.player.utils.BitmapUtils
import com.android.iflyings.player.utils.TextureUtils
import kotlinx.coroutines.*

class FontInfo(textString: String, textSize: Int) : MediaInfo() {
    private val mFontLock = Object()
    private val mTextString = textString
    private val mTextSize = textSize
    private var mPlayJob: Job? = null
    private var mTextureId = 0
    @Volatile private var isFontPlaying = false

    private fun updateBottom(endPos: Int) {
        if (endPos > 0) {
            var showLeft = windowLeft + endPos - textureWidth
            var showRight = windowLeft + endPos
            val showTop = windowBottom - textureHeight
            val showBottom = windowBottom
            showLeft = if (showLeft < windowLeft) windowLeft else showLeft
            showRight = if (showRight > windowRight) windowRight else showRight

            var texLeft = textureWidth - endPos
            var texRight = windowWidth + textureWidth - endPos
            val texTop = 0
            val texBottom = textureHeight
            texLeft = if (texLeft < 0) 0 else texLeft
            texRight = if (texRight > textureWidth) textureWidth else texRight

            notifyWindowShowChanged(Rect(showLeft, showTop, showRight, showBottom))
            notifyTextureShowChanged(Rect(texLeft, texTop, texRight, texBottom))
            //Log.i("zw","Font Media update ->show($showLeft, $showTop, $showRight, $showBottom) tex($texLeft, $texTop, $texRight, $texBottom)")
        }
    }

    private fun updateTop(endPos: Int) {
        if (endPos > 0) {
            var showLeft = windowLeft + endPos - textureWidth
            var showRight = windowLeft + endPos
            val showTop = 0
            val showBottom = textureHeight
            showLeft = if (showLeft < windowLeft) windowLeft else showLeft
            showRight = if (showRight > windowRight) windowRight else showRight

            var texLeft = textureWidth - endPos
            var texRight = windowWidth + textureWidth - endPos
            val texTop = 0
            val texBottom = textureHeight
            texLeft = if (texLeft < 0) 0 else texLeft
            texRight = if (texRight > textureWidth) textureWidth else texRight

            notifyWindowShowChanged(Rect(showLeft, showTop, showRight, showBottom))
            notifyTextureShowChanged(Rect(texLeft, texTop, texRight, texBottom))
            //Log.i("zw","Font Media update ->show($showLeft, $showTop, $showRight, $showBottom) tex($texLeft, $texTop, $texRight, $texBottom)")
        }
    }

    override fun mediaCreate() {
        BitmapUtils.loadBitmapFromText(mTextString, mTextSize).apply {
            setTextureSize(width, height)
            postInGLThread(Runnable {
                mTextureId = TextureUtils.getTextureFromBitmap(this)
                mPlayJob = GlobalScope.launch(Dispatchers.Default) {
                    recycle()
                    notifyMediaCreated()
                    var endPos = windowWidth + textureWidth
                    updateTop(endPos)
                    isFontPlaying = true
                    while (endPos > 0) {
                        synchronized(mFontLock) {
                            updateTop(endPos)
                        }
                        endPos -= 5
                        delay(100)
                    }
                    notifyMediaCompleted()
                }
            })
        }
    }

    override fun mediaDraw(textureIndex: Int): Int {
        synchronized(mFontLock) {
            if (mTextureId != 0 && isFontPlaying) {
                return drawImage(mTextureId, textureIndex)
            }
        }
        return textureIndex
    }

    override fun mediaDestroy() {
        isFontPlaying = false
        mPlayJob?.cancel()
        mPlayJob = null
        postInGLThread(Runnable {
            if (mTextureId != 0) {
                TextureUtils.unloadTexture(mTextureId)
                mTextureId = 0
            }
            notifyMediaDestroyed()
        })
    }
}