package com.android.iflyings.player.info

import com.android.iflyings.player.utils.BitmapUtils
import com.android.iflyings.player.utils.TextureUtils
import kotlinx.coroutines.*

class ImageInfo(imagePath: String) : MediaInfo() {

    private val mImagePath = imagePath
    private val mPlayTime = 5000L
    private var mPlayJob: Job? = null
    private var mTextureId = 0

    override fun mediaCreate() {
        BitmapUtils.loadBitmapFromPath(mImagePath).apply {
            setTextureSize(width, height)
            postInGLThread(Runnable {
                mTextureId = TextureUtils.getTextureFromBitmap(this)
                mPlayJob = GlobalScope.launch(Dispatchers.Default) {
                    recycle()
                    notifyMediaCreated()
                    delay(mPlayTime)
                    notifyMediaCompleted()
                }
            })
        }
    }

    override fun mediaDraw(textureIndex: Int): Int {
        if (mTextureId != 0) {
            return drawImage(mTextureId, textureIndex)
        }
        return textureIndex
    }

    override fun mediaDestroy() {
        mPlayJob?.cancel()
        mPlayJob = null
        postInGLThread(Runnable {
            if (mTextureId != 0) {
                TextureUtils.unloadTexture(mTextureId)
                mTextureId = 0
            }
            GlobalScope.launch(Dispatchers.Default) {
                notifyMediaDestroyed()
            }
        })
    }
}
