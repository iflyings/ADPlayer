package com.android.iflyings.player.info

import android.graphics.Bitmap
import com.android.iflyings.player.model.MediaModel
import com.android.iflyings.player.shader.ShaderManager
import com.android.iflyings.player.utils.BitmapUtils
import com.android.iflyings.player.utils.TextureUtils
import java.util.*

class ImageInfo(imagePath: String, winInfo: WindowInfo, listener: OnMediaListener) :
        MediaInfo(winInfo, listener) {
    private val mImagePath = imagePath
    private val playTime = 3000L
    private val mTimer = Timer()
    private var mBitmap: Bitmap? = null

    override fun mediaCreate() {
        mBitmap = BitmapUtils.loadBitmapFromPath(mImagePath)
        setTextureSize(mBitmap!!.width, mBitmap!!.height)
        notifyMediaCreated()
    }
    override fun mediaBind() {
        val textureId = TextureUtils.getTextureFromBitmap(mBitmap)
        mBitmap!!.recycle()
        notifyMediaBinded(textureId)
    }
    override fun mediaStart() {
        mTimer.schedule(object: TimerTask() {
            override fun run() {
                notifyMediaCompleted()
            }
        }, playTime)
    }
    override fun mediaDraw(mediaModel: MediaModel, textureId: Int, textureIndex: Int) {
        ShaderManager.instance.drawImage(mediaModel, textureId, textureIndex)
    }
    override fun mediaDestroy() {
        mTimer.purge()
        if (mBitmap != null) {
            mBitmap!!.recycle()
            mBitmap = null
        }
        notifyMediaDestroyed()
    }
}
