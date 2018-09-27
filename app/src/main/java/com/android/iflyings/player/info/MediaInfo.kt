package com.android.iflyings.player.info

import android.content.Context
import android.graphics.Rect
import com.android.iflyings.R
import com.android.iflyings.player.model.MediaModel
import com.android.iflyings.player.utils.TextureUtils
import java.util.*

abstract class MediaInfo internal constructor(winInfo: WindowInfo, listener: OnMediaListener) {
    protected val textureInfo = TextureInfo()
    protected val windowInfo
        get() = mMediaModel.windowInfo
    protected val textureMatrix
        get() = mMediaModel.textureMatrix
    private val mMediaModel = MediaModel(winInfo, textureInfo)

    private val mOnMediaListener = listener
    private val mAnimationType = Animation.Random
    private val mMediaLock = Object()
    private var isPlaying = false
    private var mTextureId = 0

    fun getAnimationType(): Animation {
        return mAnimationType.get()
    }

    fun create() {
        mOnMediaListener.runInUserThread(Runnable {
            synchronized(mMediaLock) {
                mediaCreate()
            }
        })
    }
    fun start() {
        synchronized(mMediaLock) {
            mediaStart()
        }
        isPlaying = true
    }
    fun draw(textureIndex: Int): Int {
        if (!isPlaying) {
            return textureIndex
        }
        synchronized(mMediaLock) {
            mediaDraw(mMediaModel, mTextureId, textureIndex)
        }
        return textureIndex + 1
    }
    fun destroy() {
        isPlaying = false
        mOnMediaListener.runInUserThread(Runnable {
            synchronized(mMediaLock) {
                mediaDestroy()
            }
        })
        mOnMediaListener.runInGLThread(Runnable {
            if (mTextureId != 0) {
                TextureUtils.unloadTexture(mTextureId)
                mTextureId = 0
            }
        })
    }

    protected abstract fun mediaCreate()
    protected abstract fun mediaBind()
    protected abstract fun mediaStart()
    protected abstract fun mediaDraw(mediaModel: MediaModel, textureId: Int, textureIndex: Int)
    protected abstract fun mediaDestroy()

    protected fun setTextureSize(texWidth: Int, texHeight: Int) {
        textureInfo.setTextureSize(texWidth, texHeight)
        mMediaModel.notifyMediaModelUpdated()
    }
    fun setTextureShow(rect: Rect?) {
        textureInfo.setTextureShow(rect)
        mMediaModel.notifyMediaModelUpdated()
    }
    fun notifyMediaModelUpdated() {
        mMediaModel.notifyMediaModelUpdated()
    }

    protected fun notifyWindowShowChanged(rect: Rect?) {
        mMediaModel.vertexBuffer.put(windowInfo.getVertexBuffer(rect)).position(0)
    }
    protected fun notifyTextureShowChanged(rect: Rect?) {
        mMediaModel.textureBuffer.put(textureInfo.getTextureBuffer(rect)).position(0)
    }

    protected fun notifyMediaCreated() {
        mOnMediaListener.runInGLThread(Runnable {
            synchronized(mMediaLock) {
                mediaBind()
            }
        })
    }
    protected fun notifyMediaBinded(textureId: Int) {
        mTextureId = textureId
        mOnMediaListener.onCreated(this)
    }
    protected fun notifyMediaCompleted() {
        mOnMediaListener.onCompleted(this)
    }
    protected fun notifyMediaDestroyed() {
        mOnMediaListener.onDestroyed(this)
    }
    protected fun notifyMediaError(message: String) {
        mOnMediaListener.onErrored(this, message)
    }

    interface OnMediaListener {

        fun runInUserThread(r: Runnable)

        fun runInGLThread(r: Runnable)

        fun onCreated(m: MediaInfo)

        fun onCompleted(m: MediaInfo)

        fun onErrored(m: MediaInfo, msg: String)

        fun onDestroyed(m: MediaInfo)

    }

    sealed class Animation {
        object Random : Animation()
        object FadeInOut : Animation()
        object ZoomIn : Animation()
        object ZoomOut : Animation()
        object RotateH : Animation()
        object RotateV : Animation()
        object CircleIn : Animation()
        object CircleOut : Animation()
        object Mosaic : Animation()
        object MoveLeft : Animation()
        object MoveTop : Animation()
        object Shutter : Animation()

        fun get(): Animation {
            return if (this != Animation.Random) { this } else {
                val random = Random().nextInt(11) + 1
                return when(random) {
                    1 -> FadeInOut
                    2 -> ZoomIn
                    3 -> ZoomOut
                    4 -> RotateH
                    5 -> RotateV
                    6 -> CircleIn
                    7 -> CircleOut
                    8 -> Mosaic
                    9 -> MoveLeft
                    10 -> MoveTop
                    else -> Shutter
                }
            }
        }
        fun normal(mediaInfo: MediaInfo, textureIndex: Int): Int {
            mediaInfo.mMediaModel.reset()
            return mediaInfo.draw(textureIndex)
        }
        fun font(mediaInfo: MediaInfo, textureIndex: Int): Int {
            mediaInfo.mMediaModel.setThreshold(0, 0, 0, true)
            return mediaInfo.draw(textureIndex)
        }
        fun update(mediaInfo1: MediaInfo?, mediaInfo2: MediaInfo?, textureIndex: Int, ratio: Float): Int {
            var index = textureIndex
            when (this) {
                FadeInOut -> {
                    if (mediaInfo1 != null) {
                        mediaInfo1.mMediaModel.reset()
                        mediaInfo1.mMediaModel.setAlpha(1 - ratio)
                        index = mediaInfo1.draw(index)
                    }
                    if (mediaInfo2 != null) {
                        mediaInfo2.mMediaModel.reset()
                        mediaInfo2.mMediaModel.setAlpha(ratio)
                        index = mediaInfo2.draw(index)
                    }
                }
                ZoomIn -> {
                    if (mediaInfo2 != null) {
                        mediaInfo2.mMediaModel.reset()
                        index = mediaInfo2.draw(index)
                    }
                    if (mediaInfo1 != null) {
                        mediaInfo1.mMediaModel.reset()
                        mediaInfo1.mMediaModel.setScale(1.0f - ratio, 1.0f - ratio, 1.0f)
                        index = mediaInfo1.draw(index)
                    }
                }
                ZoomOut -> {
                    if (mediaInfo1 != null) {
                        mediaInfo1.mMediaModel.reset()
                        index = mediaInfo1.draw(index)
                    }
                    if (mediaInfo2 != null) {
                        mediaInfo2.mMediaModel.reset()
                        mediaInfo2.mMediaModel.setScale(ratio, ratio, 1.0f)
                        index = mediaInfo2.draw(index)
                    }
                }
                RotateH -> {
                    if (ratio < 0.5f) {
                        if (mediaInfo1 != null) {
                            mediaInfo1.mMediaModel.reset()
                            mediaInfo1.mMediaModel.setRotate(180 * ratio, 0.0f, 1.0f, 0.0f)
                            index = mediaInfo1.draw(index)
                        }
                    } else {
                        if (mediaInfo2 != null) {
                            mediaInfo2.mMediaModel.reset()
                            mediaInfo2.mMediaModel.setRotate(-180 * (1 - ratio), 0.0f, 1.0f, 0.0f)
                            index = mediaInfo2.draw(index)
                        }
                    }
                }
                RotateV -> {
                    if (ratio < 0.5f) {
                        if (mediaInfo1 != null) {
                            mediaInfo1.mMediaModel.reset()
                            mediaInfo1.mMediaModel.setRotate(180 * ratio, 1.0f, 0.0f, 0.0f)
                            index = mediaInfo1.draw(index)
                        }
                    } else {
                        if (mediaInfo2 != null) {
                            mediaInfo2.mMediaModel.reset()
                            mediaInfo2.mMediaModel.setRotate(-180 * (1 - ratio), 1.0f, 0.0f, 0.0f)
                            index = mediaInfo2.draw(index)
                        }
                    }
                }
                CircleIn -> {
                    if (mediaInfo2 != null) {
                        mediaInfo2.mMediaModel.reset()
                        index = mediaInfo2.draw(index)
                    }
                    if (mediaInfo1 != null) {
                        mediaInfo1.mMediaModel.reset()
                        mediaInfo1.mMediaModel.setCircle(1 - ratio)
                        index = mediaInfo1.draw(index)
                    }
                }
                CircleOut -> {
                    if (mediaInfo1 != null) {
                        mediaInfo1.mMediaModel.reset()
                        index = mediaInfo1.draw(index)
                    }
                    if (mediaInfo2 != null) {
                        mediaInfo2.mMediaModel.reset()
                        mediaInfo2.mMediaModel.setCircle(ratio)
                        index = mediaInfo2.draw(index)
                    }
                }
                Mosaic -> {
                    if (ratio < 0.5f) {
                        if (mediaInfo1 != null) {
                            mediaInfo1.mMediaModel.reset()
                            mediaInfo1.mMediaModel.setMosaic(ratio)
                            index = mediaInfo1.draw(index)
                        }
                    } else {
                        if (mediaInfo2 != null) {
                            mediaInfo2.mMediaModel.reset()
                            mediaInfo2.mMediaModel.setMosaic(1 - ratio)
                            index = mediaInfo2.draw(index)
                        }
                    }
                }
                MoveLeft -> {
                    if (mediaInfo2 != null) {
                        mediaInfo2.mMediaModel.reset()
                        index = mediaInfo2.draw(index)
                    }
                    if (mediaInfo1 != null) {
                        mediaInfo1.mMediaModel.reset()
                        mediaInfo1.mMediaModel.setRect(1 - ratio, 1.0f)
                        index = mediaInfo1.draw(index)
                    }
                }
                MoveTop -> {
                    if (mediaInfo2 != null) {
                        mediaInfo2.mMediaModel.reset()
                        index = mediaInfo2.draw(index)
                    }
                    if (mediaInfo1 != null) {
                        mediaInfo1.mMediaModel.reset()
                        mediaInfo1.mMediaModel.setRect(1.0f, 1 - ratio)
                        index = mediaInfo1.draw(index)
                    }
                }
                Shutter -> {
                    if (mediaInfo1 != null) {
                        mediaInfo1.mMediaModel.reset()
                        index = mediaInfo1.draw(index)
                    }
                    if (mediaInfo2 != null) {
                        mediaInfo2.mMediaModel.reset()
                        mediaInfo2.mMediaModel.setShutter(ratio)
                        index = mediaInfo2.draw(index)
                    }
                }
            }
            return index
        }
    }

    companion object {

        fun from(context: Context, filePath: String, winInfo: WindowInfo, listener: OnMediaListener): MediaInfo {
            var filters = context.resources.getStringArray(R.array.type_image)
            for (f in filters) {
                if (filePath.endsWith(f, true)) {
                    return ImageInfo(filePath, winInfo, listener)
                }
            }
            filters = context.resources.getStringArray(R.array.type_video)
            for (f in filters) {
                if (filePath.endsWith(f, true)) {
                    return VideoInfo(context, filePath, winInfo, listener)
                }
            }
            throw IllegalStateException("file:$filePath is not a media")
        }

    }
}
