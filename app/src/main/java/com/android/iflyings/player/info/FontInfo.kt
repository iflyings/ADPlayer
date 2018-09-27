package com.android.iflyings.player.info

import android.graphics.Bitmap
import android.graphics.Rect
import com.android.iflyings.player.model.MediaModel
import com.android.iflyings.player.shader.ShaderManager
import com.android.iflyings.player.utils.BitmapUtils
import com.android.iflyings.player.utils.TextureUtils
import java.util.*

class FontInfo(textString: String, textSize: Int, winInfo: WindowInfo, listener: OnMediaListener) :
        MediaInfo(winInfo, listener) {
    private val mTextString = textString
    private val mTextSize = textSize
    private val mTimer = Timer()

    private var mBitmap: Bitmap? = null
    private var mEndPos = 0

    private fun update() {
        if (mEndPos > 0) {
            var showLeft = windowInfo.left + mEndPos - textureInfo.texWidth
            var showRight = windowInfo.left + mEndPos
            var showTop = windowInfo.bottom - textureInfo.texHeight
            var showBottom = windowInfo.bottom
            showLeft = if (showLeft < windowInfo.left) windowInfo.left else showLeft
            showRight = if (showRight > windowInfo.right) windowInfo.right else showRight

            var texLeft = textureInfo.texWidth - mEndPos
            var texRight = windowInfo.width + textureInfo.texWidth - mEndPos
            var texTop = 0
            var texBottom = textureInfo.texHeight
            texLeft = if (texLeft < 0) 0 else texLeft
            texRight = if (texRight > textureInfo.texWidth) textureInfo.texWidth else texRight

            notifyWindowShowChanged(Rect(showLeft, showTop, showRight, showBottom))
            notifyTextureShowChanged(Rect(texLeft, texTop, texRight, texBottom))
            //Log.i("zw","Font Media update ->show($showLeft, $showTop, $showRight, $showBottom) tex($texLeft, $texTop, $texRight, $texBottom)")
        }
    }

    override fun mediaCreate() {
        mBitmap = BitmapUtils.loadBitmapFromText(mTextString, mTextSize)
        setTextureSize(mBitmap!!.width, mBitmap!!.height)
        notifyMediaCreated()
    }
    override fun mediaBind() {
        val textureId = TextureUtils.getTextureFromBitmap(mBitmap)
        mBitmap!!.recycle()
        notifyMediaBinded(textureId)
    }
    override fun mediaStart() {
        mEndPos = windowInfo.width + textureInfo.width
        mTimer.schedule(object: TimerTask() {
            override fun run() {
                mEndPos -= 5
                if (mEndPos <= 0) {
                    mTimer.cancel()
                    notifyMediaCompleted()
                } else {
                    update()
                }
            }
        }, 0, 100)
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