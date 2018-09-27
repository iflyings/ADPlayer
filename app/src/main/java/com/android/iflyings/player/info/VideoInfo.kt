package com.android.iflyings.player.info

import android.content.Context
import android.graphics.SurfaceTexture
import android.media.AudioManager
import android.view.Surface
import com.android.iflyings.player.model.MediaModel
import com.android.iflyings.player.shader.ShaderManager
import java.io.IOException
import com.android.iflyings.player.utils.TextureUtils
import tv.danmaku.ijk.media.player.IMediaPlayer
import tv.danmaku.ijk.media.player.IjkMediaPlayer

class VideoInfo(context: Context, videoPath: String, winInfo: WindowInfo, listener: OnMediaListener) :
        MediaInfo(winInfo, listener), SurfaceTexture.OnFrameAvailableListener {

    override fun mediaCreate() {
        isUpdateTexture = false
        mMediaPlayer = openPlayer()
        mMediaPlayer!!.setOnPreparedListener { mp ->
            mp.pause()
            notifyMediaCreated()
        }
    }
    override fun mediaBind() {
        if (mMediaPlayer != null) {
            val textureId = TextureUtils.getTextureFromVideo()
            mSurfaceTexture = SurfaceTexture(textureId)
            mSurfaceTexture!!.setOnFrameAvailableListener(this)
            val surface = Surface(mSurfaceTexture)
            mMediaPlayer!!.setSurface(surface)
            surface.release()
            notifyMediaBinded(textureId)
        }
    }
    override fun mediaStart() {
        if (mMediaPlayer != null) {
            //mAudioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
            mMediaPlayer!!.setOnCompletionListener { _ ->
                //mAudioManager.abandonAudioFocus(null)
                mSurfaceTexture!!.setOnFrameAvailableListener(null)
                notifyMediaCompleted()
            }
            mMediaPlayer!!.setOnErrorListener { _, what, extra ->
                //mAudioManager.abandonAudioFocus(null)
                mSurfaceTexture!!.setOnFrameAvailableListener(null)
                notifyMediaError("$mVideoPath Error:what = $what,extra = $extra")
                return@setOnErrorListener true
            }
            mMediaPlayer!!.start()
        }
    }
    override fun mediaDraw(mediaModel: MediaModel, textureId: Int, textureIndex: Int) {
        if (isUpdateTexture) {
            mSurfaceTexture!!.updateTexImage()//更新纹理
            mSurfaceTexture!!.getTransformMatrix(textureMatrix)
            isUpdateTexture = false
        }
        ShaderManager.instance.drawVideo(mediaModel, textureId, textureIndex)
    }
    override fun mediaDestroy() {
        if (mMediaPlayer != null) {
            mMediaPlayer!!.release()
            mMediaPlayer = null
        }
        notifyMediaDestroyed()
    }

    private val mVideoPath: String = videoPath
    private val mAudioManager: AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    @Volatile private var isUpdateTexture: Boolean = false
    private var mSurfaceTexture: SurfaceTexture? = null
    private var mMediaPlayer: IMediaPlayer? = null

    private fun openPlayer(): IMediaPlayer {
        val mediaPlayer = IjkMediaPlayer()
        IjkMediaPlayer.native_setLogLevel(IjkMediaPlayer.IJK_LOG_DEBUG)
        //mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1)
        //mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 1)
        //mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-handle-resolution-change", 1)

        try {
            mediaPlayer.dataSource = mVideoPath
        } catch (e: IOException) {
            e.printStackTrace()
        }
        mediaPlayer.isLooping = false
        mediaPlayer.setOnVideoSizeChangedListener { mp, width, height, sar_num, sar_den -> setTextureSize(width, height) }
        mediaPlayer.prepareAsync()
        return mediaPlayer
    }

    override fun onFrameAvailable(surfaceTexture: SurfaceTexture) {
        isUpdateTexture = true
    }
}
