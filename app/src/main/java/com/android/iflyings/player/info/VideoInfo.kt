package com.android.iflyings.player.info

import android.graphics.SurfaceTexture
import android.view.Surface
import com.android.iflyings.player.VideoPlayer
import com.android.iflyings.player.utils.TextureUtils
import com.orhanobut.logger.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class VideoInfo(private val mVideoPath: String) : MediaInfo() {
    private val mVideoLock = Object()
    private var mSurfaceTexture: SurfaceTexture? = null
    private var mVideoPlayer: VideoPlayer? = null
    private var mVideoTextureId = 0
    @Volatile private var isVideoPlaying = false

    override fun mediaCreate() {
        synchronized(mVideoLock) {
            mVideoPlayer = openVideoPlayer()
            isVideoPlaying = false
        }
    }

    override fun mediaDraw(textureIndex: Int): Int {
        synchronized(mVideoLock) {
            mSurfaceTexture?.updateTexImage()//更新纹理
            if (mVideoTextureId != 0 && isVideoPlaying) {
                return drawVideo(mVideoTextureId, textureIndex)
            }
        }
        return textureIndex
    }

    override fun mediaDestroy() {
        synchronized(mVideoLock) {
            mVideoPlayer?.release()
            mVideoPlayer = null
            mSurfaceTexture?.release()
            mSurfaceTexture = null
            isVideoPlaying = false
        }
        postInGLThread(Runnable {
            synchronized(mVideoLock) {
                if (mVideoTextureId != 0) {
                    TextureUtils.unloadTexture(mVideoTextureId)
                    mVideoTextureId = 0
                }
            }
            notifyMediaDestroyed()
        })
    }


    private fun bindVideoPlayer(videoPlayer: VideoPlayer, textureId: Int) {
        GlobalScope.launch(Dispatchers.Default) {
            synchronized(mVideoLock) {
                val surfaceTexture = SurfaceTexture(textureId)
                val surface = Surface(surfaceTexture)
                videoPlayer.setOutputSurface(surface)
                videoPlayer.start()
                mSurfaceTexture = surfaceTexture
            }
        }
    }
    private fun openVideoPlayer(): VideoPlayer {
        return VideoPlayer().apply {
            setVideoPath(mVideoPath)
            setVideoPlayerCallback(object: VideoPlayer.VideoPlayerCallback {
                override fun videoInfo(width: Int, height: Int, during: Long, matrix: FloatArray) {
                    setTextureSize(width, height)
                    for (index in 0 until 16) {
                        textureMatrix[index] = matrix[index]
                    }
                }
                override fun noAudioData() {

                }
                override fun noVideoData() {
                    notifyMediaFailed("it is not a video")
                }
                override fun playStarted() {
                    isVideoPlaying = true
                    notifyMediaCreated()
                }
                override fun playCompleted() {
                    synchronized(mVideoLock) {
                        mVideoPlayer?.release()
                        mVideoPlayer = null
                        mSurfaceTexture?.release()
                        mSurfaceTexture = null
                    }
                    notifyMediaCompleted()
                }
            })
            postInGLThread(Runnable {
                synchronized(mVideoLock) {
                    mVideoTextureId = TextureUtils.createVideoTexture()
                }
                bindVideoPlayer(this, mVideoTextureId)
            })
        }
    }
/*
    private fun bindSurfaceTexture(mediaPlayer: IMediaPlayer, textureId: Int) {
        postInUserThread(Runnable {
            synchronized(this) {
                val surfaceTexture = SurfaceTexture(textureId)
                surfaceTexture.setOnFrameAvailableListener {
                    Logger.i("setOnFrameAvailableListener = $mVideoPath")
                    isUpdateTexture = true
                }
                val surface = Surface(surfaceTexture)
                mediaPlayer.setSurface(surface)
                surface.release()
                mSurfaceTexture = surfaceTexture
            }
        })
    }
    private fun openIjkPlayer(): IMediaPlayer {
        IjkMediaPlayer.native_setLogLevel(IjkMediaPlayer.IJK_LOG_DEBUG)
        return IjkMediaPlayer().apply {
            //setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1)
            //setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 1)
            //setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-handle-resolution-change", 1)
            dataSource = mVideoPath
            isLooping = false
            setOnVideoSizeChangedListener { _, width, height, _, _ -> setTextureSize(width, height) }
            setOnPreparedListener {
                postInGLThread(Runnable {
                    mVideoTextureId = TextureUtils.getTextureFromVideo()
                    bindSurfaceTexture(this, mVideoTextureId)
                })
                isMediaPrepared = true
                if (isVideoPlaying) {
                    it.start()
                } else {
                    it.pause()
                    //mp.seekTo(100)
                }
            }
            setOnCompletionListener {
                synchronized(this) {
                    isMediaPrepared = false
                    it.release()
                    mMediaPlayer = null
                    //mAudioManager.abandonAudioFocus(null)
                    mSurfaceTexture?.release()
                    mSurfaceTexture = null
                }
                notifyMediaCompleted()
            }
            setOnErrorListener { _, what, extra ->
                notifyMediaFailed("$mVideoPath Error:what = $what,extra = $extra")
                return@setOnErrorListener false
            }
            prepareAsync()
        }
    }
*/
}