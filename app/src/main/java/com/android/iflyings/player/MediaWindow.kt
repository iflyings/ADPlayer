package com.android.iflyings.player

import android.graphics.Rect
import com.android.iflyings.player.info.FontInfo
import com.android.iflyings.player.info.MediaInfo
import com.android.iflyings.player.model.WindowData
import com.android.iflyings.player.transformer.FontTransformer
import com.orhanobut.logger.Logger
import kotlinx.coroutines.*

class MediaWindow(callback: WindowCallback) {

    private val mWindowData = WindowData()
    private val mWindowCallback = callback
    private val mScroller = MediaScroller()

    private val mAllMediaLists = mutableListOf<MediaInfo>()
    private var mMediaModeIndex = 0

    private val mMediaLock = Object()
    private var mFontHolder: MediaHolder? = null
    private var mNowMedia: MediaHolder? = null
    private var mNextMedia: MediaHolder? = null

    private var mMediaTransformer: MediaTransformer? = null
    private var mAnimationJob: Job? = null

    data class MediaHolder(val mediaInfo: MediaInfo, val itemIndex: Int) {
        init {
            GlobalScope.launch(Dispatchers.Default) {
                mediaInfo.mediaCreate()
            }
        }

        fun mediaDraw(transformer: MediaTransformer, textureIndex: Int): Int {
            transformer.transformMedia(mediaInfo, position)
            return mediaInfo.draw(textureIndex)
        }

        fun mediaDestroy() {
            GlobalScope.launch(Dispatchers.Default) {
                mediaInfo.mediaDestroy()
            }
        }

        var position = -1f
    }

    fun start() {
        mMediaModeIndex = 0
        if (mAllMediaLists.size > 0) {
            mNextMedia = MediaHolder(mAllMediaLists[mMediaModeIndex], mMediaModeIndex)
        }
    }
    fun stop() {
        mAnimationJob?.cancel()
        mFontHolder?.mediaDestroy()
        mFontHolder = null
        for (mediaInfo in mAllMediaLists) {
            mediaInfo.mediaDestroy()
        }
        mAllMediaLists.clear()
    }
    fun add(mediaInfo: MediaInfo) {
        mediaInfo.setWindowData(mWindowData)
        mediaInfo.setOnMediaListener(object: MediaInfo.OnMediaListener {
            override fun runInGLThread(r: Runnable) {
                mWindowCallback.runInGLThread(r)
            }
            override fun onCreated(m: MediaInfo) {
                mMediaTransformer = m.getMediaTransformer()
                startAnimation()
            }
            override fun onCompleted(m: MediaInfo) {
                synchronized(mMediaLock) {
                    val nextItem = (mMediaModeIndex + 1) % mAllMediaLists.size
                    mNextMedia = MediaHolder(mAllMediaLists[nextItem], nextItem)
                }
            }
            override fun onFailed(m: MediaInfo, msg: String) {
                synchronized(mMediaLock) {
                    val nextItem = (mMediaModeIndex + 1) % mAllMediaLists.size
                    mNextMedia = MediaHolder(mAllMediaLists[nextItem], nextItem)
                }
            }
            override fun onDestroyed(m: MediaInfo) {

            }
        })
        mAllMediaLists.add(mediaInfo)
    }
    fun setScreenSize(width: Int, height: Int) {
        mWindowData.setScreenSize(width, height)
        for (mediaInfo in mAllMediaLists) {
            mediaInfo.notifyMediaModelUpdated()
        }
    }
    fun setWindowSize(rect: Rect?) {
        mWindowData.setWindowSize(rect)
        for (mediaInfo in mAllMediaLists) {
            mediaInfo.notifyMediaModelUpdated()
        }
    }

    private fun updatePosition(p: Float) {
        mNowMedia?.apply {
            this.position = -p
        }
        mNextMedia?.apply {
            this.position = 1 - p
        }
    }
    private fun startAnimation() {
        mScroller.startScroll(0f, 3000)
        mAnimationJob = GlobalScope.launch(Dispatchers.Default) {
            while (mScroller.computeScrollOffset()) {
                synchronized(mMediaLock) {
                    updatePosition(mScroller.currX)
                }
                delay(16)
            }
            synchronized(mMediaLock) {
                mNowMedia?.mediaDestroy()
                mNowMedia = mNextMedia
                mNextMedia = null
                mMediaModeIndex = (mMediaModeIndex + 1) % mAllMediaLists.size
            }
            mAnimationJob = null
        }
    }

    fun setFontInfo(textString: String, textSize: Int) {
        mFontHolder?.mediaDestroy()
        val fontInfo = FontInfo(textString, textSize).apply {
            setWindowData(mWindowData)
            setOnMediaListener(object : MediaInfo.OnMediaListener {
                override fun runInGLThread(r: Runnable) {
                    mWindowCallback.runInGLThread(r)
                }
                override fun onCreated(m: MediaInfo) {
                }
                override fun onCompleted(m: MediaInfo) {
                    mFontHolder?.mediaDestroy()
                    mFontHolder = null
                }
                override fun onFailed(m: MediaInfo, msg: String) {
                    mFontHolder?.mediaDestroy()
                    mFontHolder = null
                }
                override fun onDestroyed(m: MediaInfo) {
                    mFontHolder?.mediaDestroy()
                    mFontHolder = null
                }
            })
        }
        mFontHolder = MediaHolder(fontInfo, -1)
    }

    private fun drawFontInfo(textureIndex: Int): Int {
        var index = textureIndex
        mFontHolder?.apply {
            index = mediaDraw(FontTransformer(), textureIndex)
        }
        return index
    }
    private fun drawMediaInfo(textureIndex: Int): Int {
        mMediaTransformer?.let {
            synchronized(mMediaLock) {
                var index = textureIndex
                mNowMedia?.apply {
                    index = mediaDraw(it, index)
                }
                mNextMedia?.apply {
                    index = mediaDraw(it, index)
                }
                return index
            }
        }
        return textureIndex
    }
    // 在 GLThread 中运行
    fun draw(textureIndex: Int): Int {
        var index = textureIndex
        index = drawMediaInfo(index)
        index = drawFontInfo(index)
        return index
    }

    interface MediaTransformer {

        fun transformMedia(mediaInfo: MediaInfo, position: Float)

    }

    interface WindowCallback {

        fun showMessage(msg: String)

        fun runInGLThread(r: Runnable)

    }

}
