package com.android.iflyings.player

import android.content.Context
import android.graphics.Rect
import android.os.Looper
import com.android.iflyings.player.info.FontInfo
import com.android.iflyings.player.info.MediaInfo
import com.android.iflyings.player.info.WindowInfo

class MediaWindow(callback: WindowCallback) :
        Thread(), MediaInfo.OnMediaListener, AnimationHandler.OnAnimationListener {

    override fun runInUserThread(r: Runnable) {
        synchronized(mHandlerLock) {
            mHandler?.post(r)
        }
    }
    override fun runInGLThread(r: Runnable) {
        mCallback.runInGLThread(r)
    }
    override fun onCreated(m: MediaInfo) {
        if (m is FontInfo) {
            synchronized(mFontLock) {
                m.start()
                isDrawFontInfo = true
            }
        } else {
            if (!isStartPlaying) {
                synchronized(mHandlerLock) {
                    mHandler?.scroll(40)
                }
                isStartPlaying = true
            }
        }
    }
    override fun onCompleted(m: MediaInfo) {
        if (m is FontInfo) {
            synchronized(mFontLock) {
                isDrawFontInfo = false
                mFontInfo!!.destroy()
                mFontInfo = null
            }
        } else {
            completion()
        }
    }
    override fun onErrored(m: MediaInfo, msg: String) {
        mCallback.showMessage(msg)
        completion()
    }
    override fun onDestroyed(m: MediaInfo) {

    }

    override fun startAnimation() {
        nextModel!!.start()
        mAnimationType = nextModel!!.getAnimationType()
        mAnimationRatio = 0.0f
    }
    override fun updateAnimation(ratio: Float) {
        mAnimationRatio = ratio
    }
    override fun endAnimation() {
        val tmpMedia = nowModel
        synchronized(mModelLock) {
            mAnimationRatio = 1.0f
            nowModel = nextModel
            nextModel = mMediaInfoLists[mMediaModeIndex]
            mMediaModeIndex = (mMediaModeIndex + 1) % mMediaInfoLists.size
        }
        tmpMedia?.destroy()
        nextModel!!.create()
    }

    private val mWindowInfo = WindowInfo()

    private val mHandlerLock = Object()
    private var mHandler: AnimationHandler? = null

    private val mCallback = callback
    private val mMediaInfoLists = mutableListOf<MediaInfo>()
    private var mMediaModeIndex = 0
    private lateinit var mAnimationType: MediaInfo.Animation

    private var mFontInfo: FontInfo? = null
    private val mFontLock = Object()
    private val mModelLock = Object()
    private var nowModel: MediaInfo? = null
    private var nextModel: MediaInfo? = null
    @Volatile private var mAnimationRatio = -1.0f
    @Volatile private var isStartPlaying = false
    @Volatile private var isDrawFontInfo = false

    private fun completion() {
        synchronized(mHandlerLock) {
            mHandler?.scroll(40)
        }
    }

    override fun run() {
        super.run()
        Looper.prepare()
        mHandler = AnimationHandler(this)

        nextModel = mMediaInfoLists[mMediaModeIndex]
        mMediaModeIndex = (mMediaModeIndex + 1) % mMediaInfoLists.size
        mAnimationType = nextModel!!.getAnimationType()
        nextModel!!.create()

        Looper.loop()
    }

    fun release() {
        isStartPlaying = false
        mFontInfo?.destroy()
        for (mediaInfo in mMediaInfoLists) {
            mediaInfo.destroy()
        }
        synchronized(mHandlerLock) {
            mHandler?.looper?.quitSafely()
            mHandler = null
        }
        //mHandler.removeCallbacksAndMessages(null)
    }
    fun add(context: Context, filePath: String, rect: Rect? = null) {
        val mediaInfo = MediaInfo.from(context, filePath, mWindowInfo, this)
        mediaInfo.setTextureShow(rect)
        mMediaInfoLists.add(mediaInfo)
    }
    fun setScreenSize(width: Int, height: Int) {
        mWindowInfo.setScreenSize(width, height)
    }
    fun setWindowSize(rect: Rect?) {
        mWindowInfo.setWindowSize(rect)
        for (mediaInfo in mMediaInfoLists) {
            mediaInfo.notifyMediaModelUpdated()
        }
    }
    fun setFontInfo(textString: String, textSize: Int) {
        val tmpInfo = mFontInfo
        synchronized (mFontLock) {
            isDrawFontInfo = false
            mFontInfo = FontInfo(textString, textSize, mWindowInfo, this)
        }
        tmpInfo?.destroy()
        mFontInfo!!.create()
    }

    private fun drawFontInfo(textureIndex: Int): Int {
        synchronized(mFontLock) {
            if (isDrawFontInfo) {
                return mAnimationType.font(mFontInfo!!, textureIndex)
            }
            return textureIndex
        }
    }
    private fun drawMediaInfo(textureIndex: Int): Int {
        var index = textureIndex
        synchronized(mModelLock) {
            if (mAnimationRatio >= 1.0f) {
                if (nowModel != null) {
                    index = mAnimationType.normal(nowModel!!, textureIndex)
                }
            } else {
                index = mAnimationType.update(nowModel, nextModel, index, mAnimationRatio)
            }
        }
        return index
    }
    // 在 GLThread 中运行
    fun draw(textureIndex: Int): Int {
        var index = textureIndex
        if (!isStartPlaying) {
            return index
        }
        index = drawMediaInfo(index)
        index = drawFontInfo(index)
        return index
    }

    interface WindowCallback {

        fun showMessage(msg: String)

        fun runInGLThread(r: Runnable)

    }

}
